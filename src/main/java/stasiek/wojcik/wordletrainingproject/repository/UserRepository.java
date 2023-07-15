package stasiek.wojcik.wordletrainingproject.repository;

import stasiek.wojcik.wordletrainingproject.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findUserByUsername(final String username);
}
