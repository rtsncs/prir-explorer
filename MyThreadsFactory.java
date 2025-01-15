import java.util.AbstractMap;
import java.util.Map;

public class MyThreadsFactory implements ThreadsFactory {
	int readers;
	int returned_readers = 0;
	Position2D[] positions;

	MyThreadsFactory(int readers, Position2D[] positions) {
		this.readers = readers;
		this.positions = positions;
	}

	public int readersThreads() {
		return readers;
	}

	public Map.Entry<Thread, Position2D> readerThread(Runnable run) {
		if (returned_readers >= readers)
			return null;
		return new AbstractMap.SimpleEntry<>(new Thread(run), positions[returned_readers++]);
	}

	public Thread writterThread(Runnable run) {
		return new Thread(run);
	}
}
