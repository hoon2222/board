package com.example.board.dao;

import com.example.board.dto.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class UserDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsertOperations insertUser;

    public UserDao(DataSource dataSource){
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        insertUser = new SimpleJdbcInsert(dataSource)
                .withTableName("user")
                .usingGeneratedKeyColumns("user_id")
                .usingColumns("email", "name", "password");
    }

    @Transactional
    public User addUser(String email, String name, String password){
        User user = new User();
        user.setName(name); // name 칼럼.
        user.setEmail(email); // email
        user.setPassword(password); // password

        //유저추가쿼리
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);

        Number number = insertUser.executeAndReturnKey(params);// insert를 실행하고, 자동으로 생성된 id를 가져온다.
        int userId = number.intValue();
        user.setUserId(userId);
        return user;
    }
    @Transactional
    public void mappingUserRole(int userId){
        // insert into user_role( user_id, role_id ) values ( ?, 1);
        String sql = "insert into user_role( user_id, role_id ) values (:userId, 1)";
        SqlParameterSource params = new MapSqlParameterSource("userId", userId);
        jdbcTemplate.update(sql, params);

    }

    @Transactional
    public User getUser(String email) {
        try {
            // user_id => setUserId , email => setEmail ...
            String sql = "select user_id, email, name, password, regdate from user where email = :email";
            SqlParameterSource params = new MapSqlParameterSource("email", email);
            RowMapper<User> rowMapper = BeanPropertyRowMapper.newInstance(User.class);
            User user = jdbcTemplate.queryForObject(sql, params, rowMapper);
            return user;
        }catch(Exception ex){
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<String> getRoles(int userId) {
        String sql = "select r.name from user_role ur, role r where ur.role_id = r.role_id and ur.user_id = :userId";

        List<String> roles = jdbcTemplate.query(sql, Map.of("userId", userId), (rs, rowNum) -> {
            return rs.getString(1);
        });
        return roles;
    }
}