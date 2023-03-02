package util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Password {
	public static void main(String[] args) {

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		String password1 = passwordEncoder.encode("user-1");
		String password2 = passwordEncoder.encode("user-2");

		System.out.println(password1); // $2a$10$Rni2NWvPgpw7n5ybC39uROxnoQTnIiBJgamO30ZWOD/yKdq7AhKIi
		System.out.println(password2); // $2a$10$h8zFDkYGncsy8rkaLs614uQ9quwXtxNLKOtLcMmRiHAL6g7bxmCtG
	}

}
