
## 4. Test Case 1
| Process | Arrival Time | Burst Time | Initial Priority |
|---|---:|---:|---:|
| P1 | 0 | 4 | 2 |
| P2 | 1 | 7 | 3 |
| P3 | 3 | 2 | 1 |

`Q = 3` and `CS = 0`. Q is ignored by SJF because there is no Round Robin quantum in this algorithm.

### 4.1 Baseline schedule (CS = 0)

At time 1, P2 arrives with 7 units while P1 has 3 units remaining. At time 3, P3 arrives with 2 units while P1 has 1 unit remaining. Neither arrival has a shorter remaining time than P1, so P1 is not preempted. P1 finishes at time 4, followed by P3 and then P2.

### 4.2 Per-process results (CS = 0)

| Process | Arrival | Burst | First Start | Finish | Waiting | Turnaround | Response |
|---|---:|---:|---:|---:|---:|---:|---:|
| P1 | 0 | 4 | 0 | 4 | 0 | 4 | 0 |
| P2 | 1 | 7 | 6 | 13 | 5 | 12 | 5 |
| P3 | 3 | 2 | 4 | 6 | 1 | 3 | 1 |
| **Average** | | | | | **2.00** | **6.33** | **2.00** |

### 4.3 Effect of changing CS

| CS | Average Waiting Time | Average Turnaround Time | Average Response Time |
|---:|---:|---:|---:|
| 0 | 2.00 | 6.33 | 2.00 |
| 1 | 3.00 | 7.33 | 3.00 |
| 2 | 4.00 | 8.33 | 4.00 |
| 3 | 5.00 | 9.33 | 5.00 |

As CS increases, both average waiting and average turnaround time increase. In this test, the execution order remains P1 → P3 → P2 for every tested CS value. The added time is overhead, not useful process execution. The later processes experience the accumulated delay because they remain ready while the CPU switches between processes.

## 5. Test Case 2

| Process | Arrival Time | Burst Time | Initial Priority |
|---|---:|---:|---:|
| P1 | 0 | 5 | 4 |
| P2 | 1 | 6 | 2 |
| P3 | 4 | 4 | 3 |

`Q = 3` and `CS = 1`.

### 5.1 Baseline schedule (CS = 1)

```text
Time:  0       5   6           10  11              17
       |  P1   |CS |    P3     |CS |       P2      |
```

P2 arrives at time 1 with 6 units while P1 has 4 remaining. P3 arrives at time 4 with 4 units while P1 has 1 remaining. Neither process is shorter than the running process at its arrival time. P1 therefore completes at time 5. A one-unit context switch occurs before P3, which is shorter than P2, runs. Another one-unit context switch occurs before P2 runs.

### 5.2 Per-process results (CS = 1)

| Process | Arrival | Burst | First Start | Finish | Waiting | Turnaround | Response |
|---|---:|---:|---:|---:|---:|---:|---:|
| P1 | 0 | 5 | 0 | 5 | 0 | 5 | 0 |
| P2 | 1 | 6 | 11 | 17 | 10 | 16 | 10 |
| P3 | 4 | 4 | 6 | 10 | 2 | 6 | 2 |
| **Average** | | | | | **4.00** | **9.00** | **4.00** |

### 5.3 Effect of changing CS

| CS | Average Waiting Time | Average Turnaround Time | Average Response Time |
|---:|---:|---:|---:|
| 0 | 3.00 | 8.00 | 3.00 |
| 1 | 4.00 | 9.00 | 4.00 |
| 2 | 5.00 | 10.00 | 5.00 |
| 3 | 6.00 | 11.00 | 6.00 |

The order remains P1 → P3 → P2 for all tested values. Each additional unit of context-switching time delays the processes waiting for the CPU. Consequently, both averages increase by one time unit per increment of CS in this particular test set.

## 6. Discussion and observations

### 6.1 Context-switching overhead

Context switching does not perform useful application work. Increasing CS therefore increases the elapsed time before waiting processes can execute. This is visible in both test cases: average waiting time and average turnaround time rise as CS increases.

The increase is workload-dependent. A switch can delay more than one ready process, so the increase in the average is not determined only by the number of switches; it also depends on how many processes are waiting during each switch.

### 6.2 Response time

Response time measures how long a process waits from arrival until its first CPU execution.
It differs from waiting time when a process runs, is preempted, and later waits again.
In these two test cases there is no preemption, so each process's waiting time equals its response time.
That equality is specific to these test cases only, not a general property of SJF.

### 6.3 Do the supplied test cases demonstrate preemption?

No. In Test Case 1, when P3 arrives, P1 has only 1 unit remaining while P3 needs 2. In Test Case 2, P1 has 4 units remaining when P2 arrives and 1 unit remaining when P3 arrives; both new processes have longer remaining times than P1 at those moments. As a result, these test cases exercise the shortest-remaining-time selection and context-switching accounting, but do not actually trigger a preemption.

A separate correctness check was run with P1 `(arrival=0, burst=5)`, P2 `(arrival=1, burst=2)`, and `CS=1`. P2 arrives while P1 has 4 units remaining, so P1 is preempted. The simulator accounts for the one-unit switch, runs P2, then switches back to P1. The resulting finish times are P2 = 4 and P1 = 9, confirming that the preemption path executes.
