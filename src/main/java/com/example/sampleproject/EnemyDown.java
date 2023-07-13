package com.example.sampleproject;

import com.example.sampleproject.command.EnemyDownCommand;
import com.example.sampleproject.command.EnemySpawnCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnemyDown extends JavaPlugin implements Listener {


    @Override
    public void onEnable() {
        EnemyDownCommand enemyDownCommand = new EnemyDownCommand(this);
        Bukkit.getPluginManager().registerEvents(enemyDownCommand, this);
        getCommand("enemyDown").setExecutor(enemyDownCommand);
        getCommand("enemyDown").setExecutor(new EnemySpawnCommand());
    }

}
