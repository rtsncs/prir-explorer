import java.util.Set;
import java.util.Stack;
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
	public OptionalInt[] visited;
	private BlockingQueue<Pair> write_queue = new LinkedBlockingQueue<Pair>();

	public void setThreadsFactory(ThreadsFactory factory) {
		this.factory = factory;
		readers = new Reader[factory.readersThreads()];
		reader_threads = new ThreadAndPosition[factory.readersThreads()];
	};

	public void setTable(Table2D table) {
		this.table = table;
		visited = new OptionalInt[table.cols() * table.rows()];
		Arrays.fill(visited, OptionalInt.empty());
	};

	public void start(int sum) {
		this.sum = sum;

		writer = new Writer();
		writer_thread = factory.writterThread(writer);
		writer_thread.start();

		for (int i = 0; i < factory.readersThreads(); i++) {
			readers[i] = new Reader();
			reader_threads[i] = factory.readerThread(readers[i]);
			readers[i].setRoot(reader_threads[i].position());
		}
		Arrays.sort(readers);
		for (int i = 0; i < readers.length; i++) {
			if (i != 0) {
				int middle_i = (readers[i - 1].root.row() * table.cols() + readers[i - 1].root.col())
						+ (readers[i].root.row() * table.cols() + readers[i].root.col()
								- readers[i - 1].root.row() * table.cols() - readers[i - 1].root.col()) / 2;
				var middle = new Position2D(middle_i % table.cols(), middle_i / table.cols());
				readers[i - 1].setStop_pos(middle);
				readers[i].setStart_pos(middle);
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
		Position2D start_pos = null;
		Position2D root;
		Position2D stop_pos = null;
		Queue<Pair> queue = new LinkedList<>();

		public void setStart_pos(Position2D start_pos) {
			this.start_pos = start_pos;
		}

		public void setStop_pos(Position2D stop_pos) {
			this.stop_pos = stop_pos;
		}

		public void setRoot(Position2D root) {
			this.root = root;
		}

		@Override
		public int compareTo(Reader other) {
			int row = root.row() - other.root.row();
			if (row == 0) {
				return root.col() - other.root.col();
			}
			return row;
		}

		@Override
		public void run() {
			if (start_pos == null) {
				start_pos = new Position2D(0, 0);
			}
			if (stop_pos == null) {
				stop_pos = new Position2D(table.cols(), table.rows());
			}
			int start_i = start_pos.row() * table.cols() + start_pos.col();
			int stop_i = stop_pos.row() * table.cols() + stop_pos.col();

			Stack<Position2D> stack = new Stack<>();
			stack.push(root);

			while (!stack.empty()) {
				var current = stack.pop();

				int i = current.row() * table.cols() + current.col();

				if (current.row() < 0 || current.row() >= table.rows() || current.col() < 0
						|| current.col() >= table.cols() || visited[i].isPresent() || i < start_i || i > stop_i)
					continue;

				visited[i] = OptionalInt.of(table.get(current));
				if (current.col() != 0) {
					check(new Pair(new Position2D(current.col() - 1, current.row()), current));
				}
				if (current.row() != 0) {
					check(new Pair(new Position2D(current.col(), current.row() - 1), current));
				}
				if (current.col() != 0 && current.row() != 0) {
					check(new Pair(new Position2D(current.col() - 1, current.row() - 1), current));
				}
				if (current.col() != table.cols() - 1 && current.row() != 0) {
					check(new Pair(new Position2D(current.col() + 1, current.row() - 1), current));
				}

				stack.push(new Position2D(current.col(), current.row() + 1));
				stack.push(new Position2D(current.col() - 1, current.row()));
				stack.push(new Position2D(current.col(), current.row() - 1));
				stack.push(new Position2D(current.col() + 1, current.row()));
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
			if (visited[i].isEmpty() || visited[j].isEmpty()) {
				queue.add(pos);
			} else if (visited[i].getAsInt() + visited[j].getAsInt() == sum) {
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
						results.add(req);
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
