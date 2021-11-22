package gui;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
public class MinesweeperAppHD extends Application {

	private static final int TILE_SIZE = 40;
	private static final int W = 800;
	private static final int H = 600;
	private static final int X_TILES = W / TILE_SIZE;
	private static final int Y_TILES = H / TILE_SIZE;
	private Tile[][] grid = new Tile[X_TILES][Y_TILES];
	private Scene scene;
	private Stage stage;
	private ArrayList<Tile> bombTiles;
	private boolean finished = false;
	private Color[] colors = {Color.BLUE, Color.GREEN, Color.RED, Color.PURPLE, Color.MAROON, Color.GRAY,
			Color.TURQUOISE};

	@Override
	public void start(Stage sstage) throws Exception {
		stage = sstage;
		stage.setTitle("Minesweeper");
		scene = new Scene(createContent());
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private Parent createContent() {
		Pane root = new Pane();
		bombTiles = new ArrayList<>();
		root.setPrefSize(W, H);
		for (int y = 0; y < Y_TILES; y++) {
			for (int x = 0; x < X_TILES; x++) {
				Tile tile = new Tile(x, y, Math.random() < 0.20);
				grid[x][y] = tile;
				root.getChildren().add(tile);
			}
		}
		for (int y = 0; y < Y_TILES; y++) {
			for (int x = 0; x < X_TILES; x++) {
				Tile tile = grid[x][y];
				if (!tile.hasBomb) {
					long bombs = getNeighbors(tile).stream().filter(t -> t.hasBomb).count();
					if (bombs > 0) {
						tile.text.setText(String.valueOf(bombs));
						tile.text.setFill(colors[(int) (bombs - 1)]);
					}
				}
			}
		}
		return root;
	}

	private List<Tile> getNeighbors(Tile tile) {
		List<Tile> neighbors = new ArrayList<>();
		int[] points = new int[]{-1, -1, -1, 0, -1, 1, 0, -1, 0, 1, 1, -1, 1, 0, 1, 1};
		for (int i = 0; i < points.length; i++) {
			int dx = points[i];
			int dy = points[++i];
			int newX = tile.x + dx;
			int newY = tile.y + dy;
			if (newX >= 0 && newX < X_TILES && newY >= 0 && newY < Y_TILES) {
				neighbors.add(grid[newX][newY]);
			}
		}
		return neighbors;
	}

	private Parent createBoom() {
		Pane root = new Pane();
		bombTiles = new ArrayList<>();
		root.setPrefSize(W, H);
		ImageView boom = new ImageView();
		boom.setImage(new Image("/images/nuclear.gif/"));
		boom.setFitHeight(H);
		boom.setFitWidth(W);
		root.getChildren().add(boom);
		scene = new Scene(root);
		stage.setScene(scene);
		return root;
	}
	private class Tile extends StackPane {

		private int x, y;
		private boolean hasBomb;
		private boolean isOpen = false;
		private Text text = new Text();
		private ImageView square, bomb, flag;

		public Tile(int x, int y, boolean hasBomb) {
			this.x = x;
			this.y = y;
			this.hasBomb = hasBomb;
			setTranslateX(x * TILE_SIZE);
			setTranslateY(y * TILE_SIZE);
			setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					MouseButton button = event.getButton();
					if (button == MouseButton.PRIMARY) {
						open();
					} else if (button == MouseButton.SECONDARY) {
						flag();
					}
				}
			});
			Image squareImage = new Image("/images/blank.png/");
			square = new ImageView();
			square.setImage(squareImage);
			square.setPreserveRatio(true);
			square.setFitHeight(TILE_SIZE);
			square.setCache(true);
			flag = new ImageView();
			getChildren().addAll(square, text, flag);
			text.setFont(Font.font("ROBOTO", FontWeight.BOLD, 28));
			text.setText(hasBomb ? "X" : "");
			text.setVisible(false);
			if (hasBomb) {
				Image bombImage = new Image("/images/mineHD.png/");
				bomb = new ImageView();
				bomb.setImage(bombImage);
				bomb.setPreserveRatio(true);
				bomb.setFitHeight(TILE_SIZE - 10);
				bomb.setVisible(false);
				getChildren().add(bomb);
				bombTiles.add(this);
			}
		}

		public void open() {
			while (!finished) {
				if (isOpen) {
					return;
				}
				isOpen = true;
				square.setImage(new Image("/images/exposed.png/"));
				if (hasBomb) {
					createBoom();
					bomb.setImage(new Image("/images/hitmineHD.png/"));
					bomb.setVisible(true);
					finished = true;
					int delay = 50;
					for (Tile t : bombTiles) {
						if (t != this) {
							t.bomb.setVisible(true);
							if (t.flag.getImage() != null) {
								t.flag.setImage(null);
								t.bomb.setImage(new Image("/images/defused.png/"));
							} else {
								t.bomb.setOpacity(0);
								FadeTransition f = new FadeTransition(Duration.seconds(0.01), t.bomb);
								f.setDelay(Duration.millis(delay));
								f.setFromValue(0);
								f.setToValue(1);
								f.play();
								f.setOnFinished(event -> t.square.setImage(new Image("/images/exposed.png/")));
								delay = delay + 50;
							}
						}
					}
				} else {
					if (text.getText().isEmpty()) {
						getNeighbors(this).forEach(Tile::open);
					} else {
						text.setVisible(true);
					}
				}
			}
		}

		public void flag() {
			if (!finished) {
				if (flag.getImage() == null) {
					flag.setImage(new Image("/images/flagHD.png/"));
					flag.setFitHeight(TILE_SIZE - 10);
					flag.setPreserveRatio(true);
					isOpen = false;
				} else {
					flag.setImage(null);
					isOpen = true;
				}
			}
		}
	}
}