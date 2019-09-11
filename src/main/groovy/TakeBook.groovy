import java.time.LocalTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TakeBook {

    static void main(String[] args) {

        Map<Integer, Integer> books = new HashMap<>()
        int numBook = 50

        for (int i in 1..numBook) {
            books.put(i, 0)
        }

        ExecutorService pool = Executors.newFixedThreadPool(numBook)

        def takeABook = { Integer userId ->
            synchronized (books) {
                if (!books.findAll { it -> it.value == 0 }) {
                    books.wait()
                }
                int bookId = books.find { it -> it.value == 0 }.key
                books[bookId] = userId
                return bookId
            }
        }

        def doneBook = { Integer bookId ->
            synchronized (books) {
                int userId = books.get(bookId)
                books[bookId] = 0
                books.notifyAll()
                return userId
            }
        }

        (1..1000).each { userId ->
            pool.submit({
                long readTime = (long) new Random().nextInt(290) + 10
                int bookId = takeABook(userId)
                println " ${LocalTime.now()} User $userId reading $bookId in $readTime s"
                TimeUnit.SECONDS.sleep(readTime)
                int _userId = doneBook(bookId)
                println "${LocalTime.now()} $_userId done $bookId"
            })
        }


    }


}
