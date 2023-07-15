
# Wordle-like-training-project

Recreating basic concepts of popular [Wordle](https://www.nytimes.com/games/wordle/index.html) game for training purposes. The words database is constant and contains over 6400 words. User has to guess 5-letter word in 6 attempts. For each valid word application will return information about correct, incorrect and misplaced letters.
The main goal was to imitate original logic of letter status recognition.

* Example for word "apple"

Simple algorithm would just recognize if letter is correct, incorrect (absent in given word) or misplaced (occurs in word but at other index). That means for guess "poppy" it would mark 2 "p" as misplaced and 1 as correct. That would be wrong as in "apple" there are only 2 "p", from which 1 is already correct. Threfore valid return should mark 1 "p" as misplaced, 1 as correct and 1 as incorrect.


## Tech Stack

* Java 17
* Spring Boot 3.0.1
* MongoDB
* JWT
* TestContainers




## Run Locally

Clone the project

```bash
  git clone https://github.com/StanislawWojcik/wordle-training-project.git
```

Application is using MongoDB, therefore user has to have Mongo up and running locally on default mongodb://localhost:27017. Before building user has to insert new file "client-secret" under src/resources. The file has to contain valid encryption (at least 256-bit).

## API Reference

#### Registration
```http
  POST /register"
```

Registers new user provied in requests body in following format:

```http
  {
    "username" : "username",
    "password" : "password"
  }
```




#### Login

```http
  POST /login
```

Returns authentication token for valid credentials provied in following format:

```http
  {
    "username" : "username",
    "password" : "password"
  }
```
Response:
```http
  {
    "token": "authentication.token"
  }
```

#### Start game

```http
  POST /start-game
```

Starts new game for user authenticated based on provided token.

#### Guess

```http
  POST /guess
```
Performs analysis for provided word:
```http
  {
    "guess" : "guess"
  }
```
Response:
```http
{
    "attempts": 2,
    "status": "IN_PROGRESS",
    "guessLetters": [
        {
            "index": 0,
            "letter": "a",
            "guessResult": "INCORRECT_POSITION"
        },
        {
            "index": 1,
            "letter": "p",
            "guessResult": "ABSENT"
        },
        {
            "index": 2,
            "letter": "p",
            "guessResult": "ABSENT"
        },
        {
            "index": 3,
            "letter": "l",
            "guessResult": "ABSENT"
        },
        {
            "index": 4,
            "letter": "e",
            "guessResult": "ABSENT"
        }
    ],
    "keyboard": {
        "a": "INCORRECT_POSITION",
        "b": "NOT_USED",
        "c": "NOT_USED",
        "d": "NOT_USED",
        "e": "ABSENT",
        "f": "NOT_USED",
        "g": "NOT_USED",
        "h": "NOT_USED",
        "i": "NOT_USED",
        "j": "NOT_USED",
        "k": "NOT_USED",
        "l": "ABSENT",
        "m": "NOT_USED",
        "n": "NOT_USED",
        "o": "ABSENT",
        "p": "ABSENT",
        "q": "NOT_USED",
        "r": "INCORRECT_POSITION",
        "s": "NOT_USED",
        "t": "ABSENT",
        "u": "NOT_USED",
        "v": "NOT_USED",
        "w": "NOT_USED",
        "x": "NOT_USED",
        "y": "NOT_USED",
        "z": "NOT_USED"
    }
}
```



## Running Tests

To run tests user has to have Docker Desktop up and running as it will be used by TestContainers to start independent instance of db.

