import java.awt.Image;

public abstract class PowerUp {
    private double x, y;
    private Image sprite;
    private Platform platform; // The platform the power-up is on
    private boolean active;
    private double width, height; // Dimensions of the power-up

    public PowerUp(double x, double y, Image sprite, Platform platform, double width, double height) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
        this.platform = platform;
        this.active = true;
        this.width = 50;
        this.height = 70;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Image getSprite() {
        return sprite;
    }

    public Platform getPlatform() {
        return platform;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public abstract void applyEffect(Game game);
}
