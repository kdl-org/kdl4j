package dev.kdl.parse.lexer;

class RingBuffer<T> {
	@SuppressWarnings("unchecked")
	public RingBuffer(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException("capacity must be a least 1");
		}
		this.items = (T[]) new Object[capacity];
	}

	public int capacity() {
		return items.length;
	}

	public int size() {
		if (head == -1) {
			return 0;
		} else if (head > tail) {
			return capacity() - head + tail + 1;
		}
		return tail - head + 1;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public T get(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size());
		}
		return items[addToIndex(head, index)];
	}

	public void addFirst(T item) {
		checkIsNotFull();
		if (head == -1) {
			head = 0;
			tail = 0;
		} else {
			head = addToIndex(head, -1);
		}
		items[head] = item;
	}

	public void addLast(T item) {
		checkIsNotFull();
		if (head == -1) {
			head = 0;
			tail = 0;
		} else {
			tail = addToIndex(tail, 1);
		}
		items[tail] = item;
	}

	private void checkIsNotFull() {
		if (size() == capacity()) {
			throw new IllegalStateException("Ring buffer is full");
		}
	}

	public T removeFirst() {
		checkIsNotEmpty();
		var item = items[head];
		items[head] = null;
		if (size() == 1) {
			head = -1;
			tail = -1;
		} else {
			head = addToIndex(head, 1);
		}
		return item;
	}

	public T removeLast() {
		checkIsNotEmpty();
		var item = items[tail];
		items[tail] = null;
		if (size() == 1) {
			head = -1;
			tail = -1;
		} else {
			tail = addToIndex(tail, -1);
		}
		return item;
	}

	private void checkIsNotEmpty() {
		if (size() == 0) {
			throw new IllegalStateException("Ring buffer is empty");
		}
	}

	private int addToIndex(int index, int amount) {
		int computedIndex = index + amount;
		return computedIndex < 0 ? computedIndex + capacity() : computedIndex % capacity();
	}

	private final T[] items;
	private int head = -1;
	private int tail = -1;
}
