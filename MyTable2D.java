public class MyTable2D implements Table2D {
	int[] table;
	boolean[] read;
	int rows;
	int cols;

	public MyTable2D(int[] table, int cols) {
		this.table = table;
		this.cols = cols;
		this.rows = table.length / cols;
		this.read = new boolean[table.length];
	}

	public int cols() {
		return cols;
	}

	public int rows() {
		return rows;
	}

	public int get(Position2D position) {
		if (read[position.row() * cols + position.col()]) {
			throw new IllegalStateException();
		}
		read[position.row() * cols + position.col()] = true;
		return table[position.row() * cols + position.col()];
	}

	public void set0(Position2D position) {
		table[position.row() * cols + position.col()] = 0;
	}
}
