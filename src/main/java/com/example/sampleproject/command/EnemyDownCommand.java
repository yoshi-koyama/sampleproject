package com.example.sampleproject.command;

import com.example.sampleproject.EnemyDown;
import com.example.sampleproject.PlayerScoreData;
import com.example.sampleproject.data.ExecutingPlayer;
import com.example.sampleproject.mapper.data.PlayerScore;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class EnemyDownCommand extends BaseCommand implements Listener {

  public static final int GAME_TIME = 20;

  public static final String EASY = "easy";
  public static final String HARD = "hard";
  public static final String NORMAL = "normal";
  public static final String NONE = "none";
  public static final String LIST = "list";


  private final EnemyDown enemyDown;
  private final PlayerScoreData playerScoreData = new PlayerScoreData();

  private final List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
  private final List<Entity> spawnEntityList = new ArrayList<>();

  public EnemyDownCommand(EnemyDown enemyDown) {

    this.enemyDown = enemyDown;
  }
  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args)
      {
    if (args.length == 1 && LIST.equals(args[0])) {
      sendPlayerScoreList(player);
      return false;
    }
    String difficulty = getDifficulty(player, args);
    if(difficulty.equals(NONE)){
      return false;
    }
    ExecutingPlayer nowExecutingPlayer = getPlayerScore(player);

    initPlayerStatus(player);

    gamePLay(player, nowExecutingPlayer, difficulty);
    return true;
  }

  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreData.selectlist();
    for(PlayerScore playerScore : playerScoreList){
        player.sendMessage(playerScore.getId() + " | "
            + playerScore.getPlayerName() + " | "
            + playerScore.getScore() + " | "
            + playerScore.getDifficulty() + " | "
            + playerScore.getRegisteredDt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    }
  }

  private Location getEnemySpawnLocation(Player player) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(1) - 3;
    int randomZ = new SplittableRandom().nextInt(1) - 3;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(player.getWorld(), x, y, z);
  }
  private EntityType getEnemy(String difficulty) {
    List<EntityType> enemyList = switch (difficulty) {
      case NORMAL -> List.of(EntityType.ZOMBIE, EntityType.SKELETON);
      case HARD -> List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITCH);
      default -> List.of(EntityType.ZOMBIE);
    };
    return enemyList.get(new SplittableRandom().nextInt(enemyList.size()));
  }
  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }

   public String getDifficulty(Player player, String[] args) {
    if (args.length == 1 && (EASY.equals(args[0]) || NORMAL.equals(args[0]) || HARD.equals(args[0]))){
      return args[0];
  }
    player.sendMessage(ChatColor.RED + "実行できません。コマンドの引数の一つ目に難易度指定が必要です。[easy,normal,hard]");
    return NONE;
}
  private ExecutingPlayer getPlayerScore(Player player) {
    ExecutingPlayer executingPlayer = new ExecutingPlayer(player.getName());

    if (executingPlayerList.isEmpty()) {
      executingPlayer = addNewPlayer(player);
    } else {
    executingPlayer =  executingPlayerList.stream()
        .findFirst()
        .map(ps -> ps.getPlayerName().equals(player.getName())
              ? ps
              : addNewPlayer(player)).orElse(executingPlayer);
    }

    executingPlayer.setGameTime(GAME_TIME);
    executingPlayer.setScore(0);
    removePotionEffect(player);
    return executingPlayer;
    }

  private static void removePotionEffect(Player player) {
    player.getActivePotionEffects().stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
  }

  private ExecutingPlayer addNewPlayer(Player player) {
    ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
    executingPlayerList.add(newPlayer);
    return newPlayer;
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();

    boolean isSpawnEnemy = spawnEntityList.stream()
        .anyMatch(entity -> entity.equals(enemy));

    if (Objects.isNull(player) || !isSpawnEnemy) {
      return;
    }

      executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst()
        .ifPresent(p -> {
          int point = switch (enemy.getType()) {
            case ZOMBIE -> 10;
            case SKELETON, WITCH -> 20;
            default -> 0;
          };
      p.setScore(p.getScore() + point);
      player.sendMessage("敵を倒した。現在のスコアは" + p.getScore() + "点をゲット。");
    });
   }
    private static void initPlayerStatus (Player player){
      player.setHealth(20);
      player.setFoodLevel(20);

      PlayerInventory inventory = player.getInventory();
      inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
      inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
      inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
      inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
      inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
    }
    private void gamePLay(Player player, ExecutingPlayer nowExecutingPlayer,String difficulty) {
    Bukkit.getScheduler().runTaskTimer(enemyDown, Runnable -> {
      if (nowExecutingPlayer.getGameTime() <= 0) {
        Runnable.cancel();

        player.sendTitle("ゲーム終わり！",
            nowExecutingPlayer.getPlayerName() + "合計 " + nowExecutingPlayer.getScore() + "点！",
            0, 60, 0);

        spawnEntityList.forEach(Entity::remove);
        spawnEntityList.clear();

        removePotionEffect(player);

        playerScoreData.insert(
        new PlayerScore(nowExecutingPlayer.getPlayerName(),
            nowExecutingPlayer.getScore(),
            difficulty));

        return;
      }
      Entity spawnEntity = player.getWorld().spawnEntity(getEnemySpawnLocation(player), getEnemy(difficulty));
      spawnEntityList.add(spawnEntity);
      nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() - 5);
    }, 0, 5 * 20);
  }
}


