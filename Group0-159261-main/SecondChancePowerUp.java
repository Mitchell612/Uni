import java.awt.Image;

public class SecondChancePowerUp extends PowerUp {
    public SecondChancePowerUp(double x, double y, Image sprite, Platform platform) {
        super(x, y, sprite, platform, 30, 30);
    }

    @Override
public void applyEffect(Game game) {
    game.setSecondChanceActive(true);  // Set the flag for second chance
    this.setActive(false);  // Consume the power-up
}
}

