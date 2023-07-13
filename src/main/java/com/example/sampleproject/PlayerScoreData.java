package com.example.sampleproject;

import com.example.sampleproject.mapper.PlayerScoreMapper;
import com.example.sampleproject.mapper.data.PlayerScore;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class PlayerScoreData {

  private SqlSessionFactory sqlSessionFactory;
  private PlayerScoreMapper mapper;

  public PlayerScoreData(){
    try {
      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
      this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      SqlSession session = sqlSessionFactory.openSession(true);
      this.mapper = session.getMapper(PlayerScoreMapper.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public List<PlayerScore> selectlist() {
      return mapper.selectList();
    }
  public void insert(PlayerScore playerScore){
    mapper.insert(playerScore);
  }
}
