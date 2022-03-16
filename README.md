# Movie Theater Seating Challenge -2020


## Overview:
Assume the movie theater has the Seating 10 rows * 20 seats.
Design and write a seat assignment program to maximize both customer satisfaction and customer safety.
public safety is assume that a buffer of seats and/or one row is required.

## Goals:

- Priority 1: Customer safety
- Priority 2: Customer satisfaction. Because if the order is more than seats in a row, we cannot seat them in a row.
- Sell maximize ticket to meet priority 1 and 2. if cannot meet priority 2, users cannot sit together in one row, minimize the groups we separate them into. But we have to keep customer safety because if tickets we sell to customers later cannot keep safety, it breaks the safety for previous orders as well.

## Assumptions:
1. "For the purpose of public safety, assume that a buffer of three seats and/or one row is required." We should meet both rules or one of them?
Assumption is to meet both of them.

2. Are they need to seat together? 
Assumption is Yes. Assume customer satisfaction is to have them seat together.
And assume in one order, they don't need public safety.

3. If can't meet both customer satisfaction and safety, what should we do?
	a. choose customer safety first. If tickets we sell to customers later cannot keep safety, it breaks the safety for previous orders. So we only sell tickets which can keep safety.
	b. if cannot meet satisfaction, minimize the groups we separate them into. 
	c. If cannot meet both, if there's remaining ticket, sell them?
 		i. pros of sell them: we can sell more tickets.
		ii. cons of sell them: the customers from previous order were satisfied but the customer who would sit with new customers wouldn't be satisfied any more.
		We choose not sell them.

4. Sell ticket in order received order?
Assumption is yes.

5. If the previous orders are fulfilled in a way the next order cannot be fulfilled, can we update the previous order?
Assumption is no. Because we are already sold it, So we just optimize the current order then we are more likely to fulfill orders in the future.


## Approach:

1. Greedy. Fill seats from row A to the end. from col 1 to the end
2. check if the user can seats together & keep safety, sit them together.
3. if there are more than one place they can sit together & safety, fill them in the place only have exactly the same seats available first.
4. if there's no place to exactly fit them and keep safety, fill them to the least available space but can fit them. we select least available space but fit.
5. if there is no one place to fit them together, split them as least as possible. put them to the largest available space. find the best place for the remaining customers. 
6. keep a priority queue with continuous safe seats. the top one is the largest continuous safe seats.
7. pop from priority queue, get the least available & safe continuous seats, fit the order into it. put the remaining continuous seat into priority queue
8. every time when pop from priority queue, check if the seats are still available & safe. if not safe, update it and put back to priority queue.
9. if requested seats more than the largest continuous safe seats, split some customers to the largest seats. then recursively try to purchase seats for the remaining users.
10. attention, if we split large request seats, when call recursive function, cannot mark unsafe seats before get all seats.


## Optimizations:
1. if we have to split large order, we can sit them in continuous rows which will be unsafe for other orders. Currently they can sit anywhere available and safe.

## How to run the code
java -classpath . MainSeating.java <input.txt>

## Potential follow ups 
1. if you get all the orders at one time and try to max satisfaction & safety, how to do that?
sort orders based on seatsRequested DESC. purchase orders based on the sorted list.
2. if we cannot meet both satisfaction and safety, we still want to sell tickets, how do we do?
we can sell unsafe seats. we marked them as 'u'. it's different from taken seats 'f'.
3. how about if the user wants to book tickets for specific seats?
4. how about if the user cancels the order?
