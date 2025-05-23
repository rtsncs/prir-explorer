public class Main {
    public static void main(String[] args) {
        MyTable2D table = new MyTable2D(
                new int[] { 1, 1, 3, 3, 2, 7, 11, 5, 0, 0, 1, 5, 1, 3, 2, 3, 0, 3, 1, 1, 11,
                        41, 0,
                        8, 9, 0, 5, 5, 0, 1, 12, 5, 1, 3, 4, 11, 11, 1, 3, 3 },
                8);

        // MyThreadsFactory factory = new MyThreadsFactory(5,
        // new Position2D[] { new Position2D(1, 4), new Position2D(2, 1),
        // new Position2D(7, 1), new Position2D(7, 2), new Position2D(4, 0) });
        MyThreadsFactory factory = new MyThreadsFactory(2,
                new Position2D[] { new Position2D(6, 2), new Position2D(2, 3), });

        ParallelExplorer explorer = new ParallelExplorer();
        explorer.setTable(table);
        explorer.setThreadsFactory(factory);
        explorer.start(10);

        while (explorer.result().isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(explorer.result());

        for (Thread read : table.read) {
            if (read == null) {
                System.out.println("Nie wszystkie pola zostały odczytane");
            }
            if (!factory.reader_threads.contains(read)) {
                System.out.println("Sus wątek odczytał");
                System.out.println(factory.reader_threads);
                System.out.println("chłopa ni ma tu");
                System.out.println(read);
                break;
            }
        }

        if (factory.writer_thread.getState() != Thread.State.TERMINATED) {
            System.out.println("Chłop nie śpi wtf");
            System.out.println(factory.writer_thread.getState());
        }

        if (factory.writer_thread != table.write) {
            System.out.println("Sus wątek zapisał");
            System.out.println(factory.writer_thread);
            System.out.println(table.write);
        }
        for (Thread read : factory.reader_threads) {
            if (read.getState() != Thread.State.TERMINATED) {
                System.out.println("Chłop czytający nie śpi wtf");
                System.out.println(read.getState());
            }
        }
        for (var cell : explorer.visited) {
            if (cell.isEmpty()) {
                System.out.println("Nie każde pole zostało skopiowane");
            }
        }
    }
}
