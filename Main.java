public class Main {
    public static void main(String[] args) {
        Table2D table = new MyTable2D(
                new int[] { 1, 1, 3, 3, 2, 7, 11, 5, 0, 0, 1, 5, 1, 3, 2, 3, 0, 3, 1, 1, 11, 41, 0,
                        8, 9, 0, 5, 5, 0, 1, 12, 5, 1, 3, 4, 11, 11, 1, 3, 3 },
                8);

        ThreadsFactory factory = new MyThreadsFactory(1, new Position2D[] { new Position2D(0, 0) });

        ParallelExplorer explorer = new ParallelExplorer();
        explorer.setTable(table);
        explorer.setThreadsFactory(factory);
        explorer.start(10);

        while (explorer.result().isEmpty()) {
        }

        System.out.println(explorer.result());
    }
}
