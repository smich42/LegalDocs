package legal;

public class ChainList<T>
{
    /* A custom data structure similar to a linked list
     * that supports chaining lists together in O(1) time */
    
    private Node head;
    private Node end;
    private Node next;

    private ChainList<T> chainedBefore;

    public ChainList()
    {
        this.head = null;
        this.end = null;
        this.next = null;
        this.chainedBefore = null;
    }

    private void addAfter(Node toAdd, Node before)
    {
        if (this.chainedBefore == null)
        {
            if (this.isEmpty())
            {
                // Initialise list for first element added
                this.head = toAdd;
                this.end = toAdd;
                this.next = toAdd;
            }
            else if (this.end == before)
            {
                // Handle node being added after end
                before.pointTo(toAdd);
                this.end = toAdd;
            }
            else
            {
                Node temp = before.next();
                
                before.pointTo(toAdd);
                toAdd.pointTo(temp);
            }
        }
        else
        {
            // If the list is chained before another list,
            // add the element to the end of the list it chained before
            chainedBefore.addAfter(toAdd, before);
        }
    }

    public void add(T val)
    {
        Node toAdd = new Node(val);
        
        this.addAfter(toAdd, this.end);
    }

    public boolean isEmpty()
    {
        return this.head == null;
    }

    public boolean hasNext()
    {
        return this.next != null;
    }

    public T getNext()
    {
        T key = this.next.getVal();
        this.next = this.next.next();

        return key; 
    }

    public void resetNext()
    {
        this.next = this.head;
    }

    public void chainTo(ChainList<T> list)
    {
        list.addAfter(this.head, list.end);
        
        list.end = this.end;
        list.chainedBefore = this;

        this.head = list.head;
    }

    public void print()
    {
        Node n = this.head;

        while (n != null)
        {
            System.out.print(n.getVal());
            n = n.next();

            if (n != null)
            {
                System.out.print(" -> ");
            }
        }
    
        System.out.println();
    }

    private class Node
    {
        private T val;
        private Node ptr;

        public Node(T val)
        {
            this.val = val;
        }

        public T getVal()
        {
            return this.val;
        }

        public void pointTo(Node next)
        {
            this.ptr = next;
        }

        public Node next()
        {
            return this.ptr;
        }
    }
}
