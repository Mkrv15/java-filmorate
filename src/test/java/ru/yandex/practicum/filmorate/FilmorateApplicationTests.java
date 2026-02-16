package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SpringBootTest
class FilmorateApplicationTests {
	User user;
	Film film;

	@BeforeEach
	void setUp() {
		user = User.builder()
				.name("name")
				.login("login")
				.email("email@yandex.ru")
				.birthday(LocalDate.of(2000, 10, 15))
				.build();
		film = Film.builder()
				.description("description")
				.name("name")
				.releaseDate(LocalDate.of(2000, 10, 15))
				.duration(120)
				.build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testValidateFilmName() {
		Assertions.assertTrue(film.isValid());

		Film filmWithNameEmpty = film;
		filmWithNameEmpty.setName("");
		Assertions.assertFalse(filmWithNameEmpty.isValid());

		Film filmWithNameNull = film;
		filmWithNameNull.setName(null);
		Assertions.assertFalse(filmWithNameNull.isValid());
	}

	@Test
	void testValidateFilmDescription() {
		Film filmWithDescriptionLength201 = film;
		filmWithDescriptionLength201.setDescription("f".repeat(201));
		Assertions.assertFalse(filmWithDescriptionLength201.isValid());

		Film filmWithDescriptionLength400 = film;
		filmWithDescriptionLength201.setDescription("f".repeat(400));
		Assertions.assertFalse(filmWithDescriptionLength400.isValid());

		Film filmWithDescriptionLength200 = film;
		filmWithDescriptionLength201.setDescription("f".repeat(200));
		Assertions.assertTrue(filmWithDescriptionLength200.isValid());

		Film filmWithDescriptionLength100 = film;
		filmWithDescriptionLength201.setDescription("f".repeat(100));
		Assertions.assertTrue(filmWithDescriptionLength100.isValid());
	}

	@Test
	void testValidateFilmReleaseDate() {
		Film filmWithReleaseDateBefore = film;
		filmWithReleaseDateBefore.setReleaseDate(Film.DATE_FIRST_FILM);
		Assertions.assertFalse(filmWithReleaseDateBefore.isValid());

		Film filmWithReleaseDateMoreBefore = film;
		filmWithReleaseDateMoreBefore.setReleaseDate(Film.DATE_FIRST_FILM.minusDays(300));
		Assertions.assertFalse(filmWithReleaseDateMoreBefore.isValid());

		Film filmWithReleaseDateAfter = film;
		filmWithReleaseDateAfter.setReleaseDate(Film.DATE_FIRST_FILM.plusDays(1));
		Assertions.assertTrue(filmWithReleaseDateAfter.isValid());

		Film filmWithReleaseDateMoreAfter = film;
		filmWithReleaseDateMoreAfter.setReleaseDate(Film.DATE_FIRST_FILM.plusDays(300));
		Assertions.assertTrue(filmWithReleaseDateMoreAfter.isValid());
	}

	@Test
	void testValidateFilmDuration() {
		Film filmWithDurationMinusOne = film;
		filmWithDurationMinusOne.setDuration(-1);
		Assertions.assertFalse(filmWithDurationMinusOne.isValid());

		Film filmWithDurationMinusTen = film;
		filmWithDurationMinusTen.setDuration(-10);
		Assertions.assertFalse(filmWithDurationMinusTen.isValid());

		Film filmWithDurationOne = film;
		filmWithDurationOne.setDuration(1);
		Assertions.assertTrue(filmWithDurationOne.isValid());

		Film filmWithDurationTen = film;
		filmWithDurationTen.setDuration(10);
		Assertions.assertTrue(filmWithDurationTen.isValid());
	}

	@Test
	void testValidateUserEmail() {
		User filmWithEmailNormal = user;
		Assertions.assertTrue(filmWithEmailNormal.isValid());

		User filmWithEmailNull = user;
		filmWithEmailNull.setEmail(null);
		Assertions.assertFalse(filmWithEmailNull.isValid());

		User filmWithEmailEmpty = user;
		filmWithEmailEmpty.setEmail("");
		Assertions.assertFalse(filmWithEmailEmpty.isValid());

		User filmWithEmailNoSymbol = user;
		filmWithEmailNoSymbol.setEmail("emailyandex.ru");
		Assertions.assertFalse(filmWithEmailNoSymbol.isValid());
	}

	@Test
	void testValidateUserLogin() {
		User filmWithLoginNormal = user;
		Assertions.assertTrue(filmWithLoginNormal.isValid());

		User filmWithLoginNull = user;
		filmWithLoginNull.setLogin(null);
		Assertions.assertFalse(filmWithLoginNull.isValid());

		User filmWithLoginEmpty = user;
		filmWithLoginEmpty.setLogin("");
		Assertions.assertFalse(filmWithLoginEmpty.isValid());

		User filmWithLoginHaveSpace = user;
		filmWithLoginHaveSpace.setLogin("lo gin");
		Assertions.assertFalse(filmWithLoginHaveSpace.isValid());
	}

	@Test
	void testValidateUserBirthday() {
		User filmWithBirthdayPast = user;
		filmWithBirthdayPast.setBirthday(LocalDate.now().minusDays(5));
		Assertions.assertTrue(filmWithBirthdayPast.isValid());

		User filmWithBirthdayFuture = user;
		filmWithBirthdayFuture.setBirthday(LocalDate.now().plusDays(5));
		Assertions.assertFalse(filmWithBirthdayFuture.isValid());
	}
}
