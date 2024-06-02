import java.util.Timer;
import java.util.TimerTask;
import java.awt.Image;

public class CollisionBypassPowerUp extends PowerUp {
    public CollisionBypassPowerUp(double x, double y, Image sprite, Platform platform) {
        super(x, y, sprite, platform, sprite.getWidth(null), sprite.getHeight(null));
    }

    @Override
    public void applyEffect(Game game) {
        game.setCollisionBypass(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                game.setCollisionBypass(false);
            }
        }, 5000);
    }
}
