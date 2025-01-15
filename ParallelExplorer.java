import java.util.Set;
import java.util.HashSet;
import java.util.OptionalInt;

public class ParallelExplorer implements Explorer {
	ThreadsFactory factory;
	Table2D table;
	Reader[] readers;
	Thread[] reader_threads;
	Writer writer;
	HashSet<Pair> results = new HashSet<>();
	int sum;
	OptionalInt[] my_table;

	public void setThreadsFactory(ThreadsFactory factory) {
		this.factory = factory;
		readers = new Reader[factory.readersThreads()];
		reader_threads = new Thread[factory.readersThreads()];
	};

	public void setTable(Table2D table) {
		this.table = table;
		my_table = new OptionalInt[table.cols() * table.rows()];
	};

	public void start(int sum) {
		this.sum = sum;
		for (int i = 0; i < factory.readersThreads(); i++) {
			readers[i] = new Reader();
			var reader_pair = factory.readerThread(readers[i]);
			reader_threads[i] = reader_pair.getKey();
			readers[i].setStart_pos(reader_pair.getValue());
			if (i != 0) {
				readers[i - 1].setStop_pos(reader_pair.getValue());
				readers[i - 1].run();
			}
			readers[factory.readersThreads() - 1].run();
		}
	};

	public Set<Pair> result() {
		for (Thread thread : reader_threads) {
			if (thread.isAlive()) {
				return new HashSet<Pair>();
			}
		}
		return results;
	};

	private class Reader implements Runnable {
		Position2D start_pos;
		Position2D stop_pos = null;
		int[] my_table;

		public void setStart_pos(Position2D start_pos) {
			this.start_pos = start_pos;
		}

		public void setStop_pos(Position2D stop_pos) {
			this.stop_pos = stop_pos;
		}

		@Override
		public void run() {
			if (stop_pos == null) {
				stop_pos = new Position2D(table.cols(), table.rows());
			}
			int rows = stop_pos.row() - start_pos.row();
			int cols = table.cols() - start_pos.col() + stop_pos.col();
			my_table = new int[rows * table.cols() + cols];

			int k = 0;
			for (int i = start_pos.row(); i < table.rows(); i++) {
				for (int j = start_pos.col(); j < table.cols(); j++) {
					if (i == stop_pos.row() && j == stop_pos.col())
						return;
					my_table[k] = table.get(new Position2D(j, i));
					if (j != 0 && my_table[k] + my_table[k - 1] == sum) {
						results.add(new Pair(new Position2D(j - 1, i), new Position2D(j, i)));
					}
					if (i != 0 && my_table[k] + my_table[k - table.cols()] == sum) {
						results.add(new Pair(new Position2D(j, i - 1), new Position2D(j, i)));
					}
					if (i != 0 && j != 0 && my_table[k] + my_table[k - table.cols() - 1] == sum) {
						results.add(new Pair(new Position2D(j - 1, i - 1), new Position2D(j, i)));
					}
					if (i != 0 && j != table.cols() - 1 && my_table[k] + my_table[k - table.cols() + 1] == sum) {
						results.add(new Pair(new Position2D(j + 1, i - 1), new Position2D(j, i)));
					}
					k++;
				}
			}
		}
	}

	private class Writer implements Runnable {
		@Override
		public void run() {
		}
	}
}
