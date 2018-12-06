package hw3.q6.queue;
import java.util.concurrent.atomic.AtomicReference;
import hw3.q6.queue.LockQueue.Node;

public class LockFreeQueue implements MyQueue {
// you are free to add members
	private Node n = new Node(null);
	private AtomicReference<Node> Head = new AtomicReference<Node>(n);
	private AtomicReference<Node> Tail = new AtomicReference<Node>(n);
	
  public LockFreeQueue() {
	// implement your constructor here
  }

  public boolean enq(Integer value) {
  	Node node = new Node(value);
  	Node tail;
  	while(true) {
  		tail = Tail.get();
  		Node next = tail.next.get();
  		if(tail==Tail.get()) {
  			if(next==null) {	// link the node to the end of the list
  				if (tail.next.compareAndSet(null, node)) {
  					break;	// Enqueue is done, exit loop
  				}
  			} else {  // Tail was not pointing to the last node
  				Tail.compareAndSet(tail, next);	// Try to swing Tail to the next node
  			}
  		}
  	}
  	Tail.compareAndSet(tail, node);
  	return true;
  }
  
  public Integer deq() throws EmptyQueue{
  	Node head;
  	Node tail;
  	Node next;
  	int pvalue;
    while(true) {
    	head = Head.get();
    	tail = Tail.get();
    	next = head.next.get();
    	if(head == Head.get()) {
    		if(head == tail) {  // queue empty OR tail falling behind
    			if (next==null) {  // queue is empty throw exception
    				throw new EmptyQueue();
    			}
    			Tail.compareAndSet(tail, next);  // tail falling behind, try to advance it
    		} else {  // no need to deal with tail
    			pvalue = next.value;	// read value before CAS, otherwise another thread will free Node(next)
    			if (Head.compareAndSet(head, next)) break;
    		}
    	}
    }
    return pvalue;
  }
  
  protected class Node {
	  private Integer value;
	  private AtomicReference<Node>  next;
		    
	  public Node(Integer x) {
		  value = x;
		  next = new AtomicReference<Node>(null);
	  }
  }
  
  @Override 
  public String toString() {
	  StringBuilder result = new StringBuilder();
	  Node temp = Head.get().next.get();
	  if (temp == null) {
		  return("Empty");
	  }
	  while(temp != null) {
		  result.append(temp.value);
		  temp = temp.next.get();
	  }
	  return result.toString();
  }
}