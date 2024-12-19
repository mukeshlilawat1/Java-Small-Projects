import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.*;

class Block {
    int x;
    int y;
    int width;
    int height;
    Image image;

    Block(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }
}

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 750;
    int boardHeight = 250;

    // Images
    Image dinosaurImage;
    Image dinosaurDeadImage;
    Image dinosaurJumpImage;
    Image cactus1Image;
    Image cactus2Image;
    Image cactus3Image;
    Image BigCactus1Image;
    Image BigCactus2Image;
    Image BigCactus3Image;
    Image Bird1Image;
    Image Bird2Image;

    // Dinosaur
    int dinosaurWidth = 88;
    int dinosaurHeight = 94;
    int dinosaurX = 50;
    int dinosaurY = boardHeight - dinosaurHeight;

    Block dinosaur;

    // Cactus
    int cactus1Width = 34;
    int cactus2Width = 69;
    int cactus3Width = 102;
    int cactusHeight = 70;
    int cactusX = 700;
    int cactusY = boardHeight - cactusHeight;
    ArrayList<Block> cactusArray;

    // Birds
    int birdWidth = 46;
    int birdHeight = 40;
    ArrayList<Block> birdArray;

    // Physics
    int velocityX = -12; // Cactus and bird speed
    int velocity = 0; // Dinosaur jump speed
    int gravity = 1;

    boolean gameOver = false;
    int score = 0;

    Timer gameLoop;
    Timer placeObstacleTimer;

    public ChromeDinosaur() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.lightGray);
        setFocusable(true);
        addKeyListener(this);

        // Load images
        dinosaurImage = new ImageIcon(getClass().getResource("./img/dino-run.gif")).getImage();
        dinosaurDeadImage = new ImageIcon(getClass().getResource("./img/dino-dead.png")).getImage();
        dinosaurJumpImage = new ImageIcon(getClass().getResource("./img/dino-jump.png")).getImage();
        cactus1Image = new ImageIcon(getClass().getResource("./img/cactus1.png")).getImage();
        cactus2Image = new ImageIcon(getClass().getResource("./img/cactus2.png")).getImage();
        cactus3Image = new ImageIcon(getClass().getResource("./img/cactus3.png")).getImage();
        BigCactus1Image = new ImageIcon(getClass().getResource("./img/big-cactus1.png")).getImage();
        BigCactus2Image = new ImageIcon(getClass().getResource("./img/big-cactus2.png")).getImage();
        BigCactus3Image = new ImageIcon(getClass().getResource("./img/big-cactus3.png")).getImage();
        Bird1Image = new ImageIcon(getClass().getResource("./img/bird1.png")).getImage();
        Bird2Image = new ImageIcon(getClass().getResource("./img/bird2.png")).getImage();

        // Initialize dinosaur
        dinosaur = new Block(dinosaurX, dinosaurY, dinosaurWidth, dinosaurHeight, dinosaurImage);

        // Initialize obstacles
        cactusArray = new ArrayList<>();
        birdArray = new ArrayList<>();

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this); // 60 FPS
        gameLoop.start();

        // Obstacle spawn timer
        placeObstacleTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeObstacle();
            }
        });
        placeObstacleTimer.start();
    }

    void placeObstacle() {
        if (gameOver) {
            return;
        }
        double obstacleChance = Math.random();

        if (obstacleChance > 0.8) {
            // Add bird
            int birdY = boardHeight - dinosaurHeight - birdHeight - (int) (Math.random() * 50);
            birdArray.add(new Block(cactusX, birdY, birdWidth, birdHeight, Math.random() > 0.5 ? Bird1Image : Bird2Image));
        } else {
            // Add cactus
            if (obstacleChance > 0.6) {
                cactusArray.add(new Block(cactusX, cactusY, cactus3Width, cactusHeight, cactus3Image));
            } else if (obstacleChance > 0.4) {
                cactusArray.add(new Block(cactusX, cactusY, cactus2Width, cactusHeight, cactus2Image));
            } else {
                cactusArray.add(new Block(cactusX, cactusY, cactus1Width, cactusHeight, cactus1Image));
            }
        }

        // Remove old obstacles
        if (cactusArray.size() > 10) {
            cactusArray.remove(0);
        }
        if (birdArray.size() > 5) {
            birdArray.remove(0);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(dinosaur.image, dinosaur.x, dinosaur.y, dinosaur.width, dinosaur.height, null);

        for (Block cactus : cactusArray) {
            g.drawImage(cactus.image, cactus.x, cactus.y, cactus.width, cactus.height, null);
        }

        for (Block bird : birdArray) {
            g.drawImage(bird.image, bird.x, bird.y, bird.width, bird.height, null);
        }

        g.setColor(Color.black);
        g.setFont(new Font("Courier", Font.BOLD, 30));
        if (gameOver) {
            g.drawString("Game Over: " + score, 10, 35);
        } else {
            g.drawString("Score: " + score, 10, 35);
        }
    }

    public void move() {
        velocity += gravity;
        dinosaur.y += velocity;

        if (dinosaur.y > dinosaurY) {
            dinosaur.y = dinosaurY;
            velocity = 0;
            dinosaur.image = dinosaurImage;
        }

        for (Block cactus : cactusArray) {
            cactus.x += velocityX;
            if (collision(dinosaur, cactus)) {
                gameOver = true;
                dinosaur.image = dinosaurDeadImage;
            }
        }

        for (Block bird : birdArray) {
            bird.x += velocityX;
            if (collision(dinosaur, bird)) {
                gameOver = true;
                dinosaur.image = dinosaurDeadImage;
            }
        }

        score++;
    }

    boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placeObstacleTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (dinosaur.y == dinosaurY) {
                velocity = -17;
                dinosaur.image = dinosaurJumpImage;
            }
            if (gameOver) {
                // Restart game
                dinosaur.y = dinosaurY;
                dinosaur.image = dinosaurImage;
                velocity = 0;
                cactusArray.clear();
                birdArray.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placeObstacleTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chrome Dinosaur Game");
        ChromeDinosaur game = new ChromeDinosaur();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}