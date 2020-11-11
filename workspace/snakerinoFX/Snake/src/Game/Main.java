package Game;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.Point;

public class Main extends Application {
    static Config config = new Config();

    private static final String[] FOODS_IMAGE = new String[]{"/img/benni.png", "/img/leo.png"};


    public enum Direction {
        right, left, up, down

    }

    private GraphicsContext gc;
    private Image foodImage;

    private int foodX;
    private int foodY;
    private int score = 0;
    private boolean gameOver;
    private Direction currentDirection = Direction.right; // Snake property

    private final Snake snake = new Snake();


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Snake");
        Group root = new Group();
        Canvas canvas = new Canvas(config.width, config.width);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        gc = canvas.getGraphicsContext2D();

        // converted object to lambda using intellij's refactor suggestion
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.RIGHT || code == KeyCode.D) {
                if (currentDirection != Direction.left) {
                    currentDirection = Direction.right;
                }
            } else if (code == KeyCode.LEFT || code == KeyCode.A) {
                if (currentDirection != Direction.right) {
                    currentDirection = Direction.left;
                }
            } else if (code == KeyCode.UP || code == KeyCode.W) {
                if (currentDirection != Direction.down) {
                    currentDirection = Direction.up;
                }
            } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                if (currentDirection != Direction.up) {
                    currentDirection = Direction.down;
                }
            } else if (code == KeyCode.ESCAPE) {
                primaryStage.close();
            }
        });

        generateFood();

        // The game animation happens because of the timeline
        // every change happens in a new keyFrame
        // and the keyframe is generated by run
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(130),
                event -> run(gc)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void run(GraphicsContext gc) {
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(new Font("Digital-7", 70));
            gc.fillText("Game Over", config.width / 3.5, config.width / 2);
            return;
        }
        drawBackground(gc);
        drawFood(gc);
        drawSnake(gc);
        drawScore();

        for (int i = snake.body.size() - 1; i >= 1; i--) {
            snake.body.get(i).x = snake.body.get(i - 1).x;
            snake.body.get(i).y = snake.body.get(i - 1).y;
        }

        switch (currentDirection) {
            case right:
                snake.moveright();
                break;
            case left:
                snake.moveLeft();
                break;
            case up:
                snake.moveUp();
                break;
            case down:
                snake.moveDown();
                break;
        }

        gameOver();
        eatFood();
    }

    private void drawBackground(GraphicsContext gc) {
        for (int i = 0; i < config.rows; i++) {
            for (int j = 0; j < config.columns; j++) {
                if ((i + j) % 2 == 0) {
                    gc.setFill(Color.web("161718"));
                } else {
                    gc.setFill(Color.web("34383B"));
                }
                gc.fillRect(i * config.square_size, j * config.square_size,
                        config.square_size, config.square_size);
            }
        }
    }

    private void generateFood() {
        while (true) {
            foodX = (int) (Math.random() * config.rows);
            foodY = (int) (Math.random() * config.columns);

            Point head = snake.body.get(0);
            if (head.getX() == foodX && head.getY() == foodY) { //foods cam appear below snake (snake shown above food)
                    continue;
            }
            foodImage = new Image(FOODS_IMAGE[(int) (Math.random() * FOODS_IMAGE.length)]);
            break;
        }
    }

    private void drawFood(GraphicsContext gc) {
        gc.drawImage(foodImage, foodX * config.square_size, foodY * config.square_size, config.square_size,
                config.square_size);
    }

    private void drawSnake(GraphicsContext gc) {
        gc.setFill(Color.web("4674E9"));
        gc.fillRoundRect(snake.head.getX() * config.square_size, snake.head.getY() * config.square_size,
                config.square_size - 1, config.square_size - 1, 35, 35);

        for (int i = 1; i < snake.body.size(); i++) {
            gc.fillRoundRect(snake.body.get(i).getX() * config.square_size,
                    snake.body.get(i).getY() * config.square_size, config.square_size - 1,
                    config.square_size - 1, 20, 20);
        }
    }


    public void gameOver() {
        if (snake.head.x < 0) {
            snake.head.x = config.rows;
        }
        else if (snake.head.y < 0){
            snake.head.y = config.columns;
        }
        else if (snake.head.x * config.square_size >= config.width){
            snake.head.x = 0;
        }
        else if (snake.head.y * config.square_size >= config.width){
            snake.head.y = 0;
        }

        //destroy itself
        for (int i = 1; i < snake.body.size(); i++) {
            if (snake.head.x == snake.body.get(i).getX() && snake.head.getY() == snake.body.get(i).getY()) {
                gameOver = true;
                break;
            }
        }
    }

    private void eatFood() {
        if (snake.head.getX() == foodX && snake.head.getY() == foodY) {
            snake.body.add(new Point(-1, -1));
            generateFood();
            score += 5;
        }
    }

    private void drawScore() {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("", 35)); // Find funny font
        gc.fillText("Score: " + score, 10, 35);
    }

    public static void main(String[] args) {
        launch(args);
    }
}