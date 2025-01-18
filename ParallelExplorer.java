import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelExplorer implements Explorer {
	private ThreadsFactory factory;
	private Table2D table;
	private Reader[] readers;
	private ThreadAndPosition[] reader_threads;
	private Writer writer;
	private Thread writer_thread;
	private HashSet<Pair> results = new HashSet<>();
	private int sum;
	public OptionalInt[] my_table;
	private BlockingQueue<Pair> write_queue = new LinkedBlockingQueue<Pair>();

	public void setThreadsFactory(ThreadsFactory factory) {
		this.factory = factory;
		readers = new Reader[factory.readersThreads()];
		reader_threads = new ThreadAndPosition[factory.readersThreads()];
	};

	public void setTable(Table2D table) {
		this.table = table;
		my_table = new OptionalInt[table.cols() * table.rows()];
		Arrays.fill(my_table, OptionalInt.empty());
	};

	public void start(int sum) {
		this.sum = sum;

		writer = new Writer();
		writer_thread = factory.writterThread(writer);
		writer_thread.start();

		for (int i = 0; i < factory.readersThreads(); i++) {
			readers[i] = new Reader();
			reader_threads[i] = factory.readerThread(readers[i]);
			readers[i].setStart_pos(reader_threads[i].position());
		}
		Arrays.sort(readers);
		for (int i = 0; i < readers.length; i++) {
			if (i != 0) {
				readers[i - 1].setStop_pos(readers[i].start_pos);
			}
		}
		for (var thread : reader_threads) {
			thread.thread().start();
		}
	};

	public Set<Pair> result() {
		if (writer_thread.getState() != Thread.State.TERMINATED) {
			return new HashSet<Pair>();
		}
		return results;
	};

	private class Reader implements Runnable, Comparable<Reader> {
		Position2D start_pos;
		Position2D stop_pos = null;
		Queue<Pair> queue = new LinkedList<>();

		public void setStart_pos(Position2D start_pos) {
			this.start_pos = start_pos;
		}

		public void setStop_pos(Position2D stop_pos) {
			this.stop_pos = stop_pos;
		}

		@Override
		public int compareTo(Reader other) {
			int row = start_pos.row() - other.start_pos.row();
			if (row == 0) {
				return start_pos.col() - other.start_pos.col();
			}
			return row;
		}

		@Override
		public void run() {
			if (stop_pos == null) {
				stop_pos = new Position2D(table.cols(), table.rows());
			}

			int j = start_pos.col();
			int k = start_pos.row() * table.cols() + start_pos.col();
			outer: for (int i = start_pos.row(); i < table.rows(); i++) {
				for (; j < table.cols(); j++) {
					if (i == stop_pos.row() && j == stop_pos.col())
						break outer;
					my_table[k] = OptionalInt.of(table.get(new Position2D(j, i)));
					if (j != 0) {
						check(new Pair(new Position2D(j - 1, i), new Position2D(j, i)));
					}
					if (i != 0) {
						check(new Pair(new Position2D(j, i - 1), new Position2D(j, i)));
					}
					if (i != 0 && j != 0) {
						check(new Pair(new Position2D(j - 1, i - 1), new Position2D(j, i)));
					}
					if (i != 0 && j != table.cols() - 1) {
						check(new Pair(new Position2D(j + 1, i - 1), new Position2D(j, i)));
					}
					k++;
				}
				j = 0;
			}
			while (!queue.isEmpty()) {
				var pos = queue.poll();
				check(pos);
			}
			try {
				write_queue.put(new Pair(null, null));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		void check(Pair pos) {
			int i = pos.first().row() * table.cols() + pos.first().col();
			int j = pos.second().row() * table.cols() + pos.second().col();
			if (my_table[i].isEmpty() || my_table[j].isEmpty()) {
				queue.add(pos);
			} else if (my_table[i].getAsInt() + my_table[j].getAsInt() == sum) {
				results.add(pos);
				try {
					write_queue.put(pos);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class Writer implements Runnable {
		int terminated_readers = 0;

		@Override
		public void run() {
			while (terminated_readers < readers.length || !write_queue.isEmpty()) {
				try {
					var req = write_queue.take();
					if (req.first() == null) {
						terminated_readers++;
					} else {
						table.set0(req.first());
						table.set0(req.second());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
