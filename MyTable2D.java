public class MyTable2D implements Table2D {
	int[] table;
	public Thread[] read;
	int rows;
	int cols;
	public Thread write;

	public MyTable2D(int[] table, int cols) {
		this.table = table;
		this.cols = cols;
		this.rows = table.length / cols;
		this.read = new Thread[table.length];
	}

	public int cols() {
		return cols;
	}

	public int rows() {
		return rows;
	}

	public int get(Position2D position) {
		if (read[position.row() * cols + position.col()] != null) {
			throw new RuntimeException(String.format("dupa: position %s, already read by thread %s, again by thread %s",
					position.toString(), read[position.row() * cols + position.col()], Thread.currentThread()));
		}
		read[position.row() * cols + position.col()] = Thread.currentThread();
		return table[position.row() * cols + position.col()];
	}

	public void set0(Position2D position) {
		table[position.row() * cols + position.col()] = 0;
		System.out.println(position);
		write = Thread.currentThread();
	}
}
