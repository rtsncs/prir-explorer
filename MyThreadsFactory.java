import java.util.ArrayList;
import java.util.List;

public class MyThreadsFactory implements ThreadsFactory {
	int readers;
	int returned_readers = 0;
	Position2D[] positions;
	public List<Thread> reader_threads;
	public Thread writer_thread;

	MyThreadsFactory(int readers, Position2D[] positions) {
		this.readers = readers;
		this.positions = positions;
		this.reader_threads = new ArrayList<>();
	}

	public int readersThreads() {
		return readers;
	}

	public ThreadAndPosition readerThread(Runnable run) {
		if (returned_readers >= readers)
			return null;
		Thread thread = new Thread(run);
		reader_threads.add(thread);
		return new ThreadAndPosition(thread, positions[returned_readers++]);
	}

	public Thread writterThread(Runnable run) {
		writer_thread = new Thread(run);
		return writer_thread;
	}
}
