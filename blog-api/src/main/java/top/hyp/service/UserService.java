package top.hyp.service;

import top.hyp.entity.User;

public interface UserService {
	User findUserByUsernameAndPassword(String username, String password);

	User findUserById(Long id);

	boolean changeAccount(User user, String jwt);
}
