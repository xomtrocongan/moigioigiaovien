package vn.teachermatch.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.teachermatch.model.AppUser;
import vn.teachermatch.model.TeacherProfile;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PortalStore High-Concurrency & Thread Safety Stress Tests")
class PortalStoreConcurrencyTest {

    @Test
    @DisplayName("Should sustain high concurrent reads and writes without ConcurrentModificationException")
    void testConcurrentReadAndWrite() throws InterruptedException {
        PortalStore store = new PortalStore();
        int threadCount = 12;
        int operationsPerThread = 80;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger errorCounter = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String user = "user_" + threadId + "_" + j;
                        // 1. Add user
                        store.addUser(user, "password123", j % 2 == 0 ? "TEACHER" : "PARTNER");

                        // 2. Authenticate
                        AppUser auth = store.authenticate(user, "password123");
                        assertNotNull(auth);

                        // 3. Log actions
                        store.log(user, "Concurrent action " + j);

                        // 4. Update status & request interviews
                        store.status(1L, j % 2 == 0 ? "READY" : "PENDING");
                        store.requestInterview(1L, "Partner_" + threadId, LocalDateTime.now().plusDays(j + 1).toString());

                        // 5. Read visible teachers & interviews
                        store.visibleTeachers("partner", "PARTNER");
                        store.visibleInterviews("PARTNER", "partner");
                        store.logs();

                        successCounter.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCounter.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finishedInTime = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finishedInTime, "Concurrent operations did not complete in time");
        assertEquals(0, errorCounter.get(), "Errors occurred during concurrent operations");
        assertEquals(threadCount * operationsPerThread, successCounter.get());
        assertTrue(store.logs().size() > threadCount * operationsPerThread);
    }
}

