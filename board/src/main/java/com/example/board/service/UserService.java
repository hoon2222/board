package com.example.board.service;

import com.example.board.dao.UserDao;
import com.example.board.dto.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor    //final 필드를 초기화하는 생성자를 자동으로 만드러짐
public class UserService {
    private final UserDao userDao;

    @Transactional
    public User addUser(String name, String email, String password){
        User user = userDao.addUser(email, name, password);
        System.out.println(user.getUserId());
        userDao.mappingUserRole(user.getUserId());
        return user;
    }

    @Transactional
    public User getUser(String email){
        return userDao.getUser(email);
    }

    @Transactional(readOnly = true)
    public List<String> getRoles(int userId) {
        return userDao.getRoles(userId);
    }
}
