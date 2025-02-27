package dev.kdl.parse.lexer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RingBufferTest {

	@Nested
	@DisplayName("addFirst(T) should")
	class AddFirst {
		@Test
		@DisplayName("add an item to an empty ring buffer")
		void emptyRingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);

			ringBuffer.addFirst("Hello, World!");

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(ringBuffer.get(0)).isEqualTo("Hello, World!");
		}

		@Test
		@DisplayName("add an item to a ring buffer of size 1")
		void size1RingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("first item");

			ringBuffer.addFirst("second item");

			assertThat(ringBuffer.size()).isEqualTo(2);
			assertThat(ringBuffer.get(0)).isEqualTo("second item");
			assertThat(ringBuffer.get(1)).isEqualTo("first item");
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is full")
		void fullRingBuffer() {
			var ringBuffer = new RingBuffer<String>(1);
			ringBuffer.addFirst("item");

			assertThatThrownBy(() -> ringBuffer.addFirst("oops"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ring buffer is full");
		}
	}

	@Nested
	@DisplayName("addLast(T) should")
	class AddLast {
		@Test
		@DisplayName("add an item to an empty ring buffer")
		void emptyRingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);

			ringBuffer.addLast("Hello, World!");

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(ringBuffer.get(0)).isEqualTo("Hello, World!");
		}

		@Test
		@DisplayName("add an item to a ring buffer of size 1")
		void size1RingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addLast("first item");

			ringBuffer.addLast("second item");

			assertThat(ringBuffer.size()).isEqualTo(2);
			assertThat(ringBuffer.get(0)).isEqualTo("first item");
			assertThat(ringBuffer.get(1)).isEqualTo("second item");
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is full")
		void fullRingBuffer() {
			var ringBuffer = new RingBuffer<String>(1);
			ringBuffer.addLast("item");

			assertThatThrownBy(() -> ringBuffer.addLast("oops"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ring buffer is full");
		}
	}

	@Nested
	@DisplayName("get(int) should")
	class Get {
		@Test
		@DisplayName("return the first item when index is 0 and buffer has one item")
		void size1RingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("item");

			var result = ringBuffer.get(0);

			assertThat(result).isEqualTo("item");
		}

		@Test
		@DisplayName("throw an IndexOutOfBoundException when index is negative")
		void negativeIndex() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("item");

			assertThatThrownBy(() -> ringBuffer.get(-1))
				.isInstanceOf(IndexOutOfBoundsException.class)
				.hasMessage("Index -1 out of bounds for size 1");
		}

		@Test
		@DisplayName("throw an IndexOutOfBoundException when index is equal to the size")
		void outOfBoundsIndex() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("item");

			assertThatThrownBy(() -> ringBuffer.get(1))
				.isInstanceOf(IndexOutOfBoundsException.class)
				.hasMessage("Index 1 out of bounds for size 1");
		}
	}

	@Nested
	@DisplayName("removeFirst() should")
	class RemoveFirst {
		@Test
		@DisplayName("remove and return the first item when the buffer has one item")
		void size1RingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("item");

			var result = ringBuffer.removeFirst();

			assertThat(ringBuffer.size()).isEqualTo(0);
			assertThat(result).isEqualTo("item");
		}

		@Test
		@DisplayName("remove and return the first item when the buffer has two items")
		void size2RingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("item1");
			ringBuffer.addLast("item2");

			var result = ringBuffer.removeFirst();

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(result).isEqualTo("item1");
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is empty")
		void emptyRingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);

			assertThatThrownBy(ringBuffer::removeFirst)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ring buffer is empty");
		}
	}

	@Nested
	@DisplayName("removeLast() should")
	class RemoveLast {
		@Test
		@DisplayName("remove and return the last item when the buffer has one item")
		void size1RingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("item");

			var result = ringBuffer.removeLast();

			assertThat(ringBuffer.size()).isEqualTo(0);
			assertThat(result).isEqualTo("item");
		}

		@Test
		@DisplayName("remove and return the first item when the buffer has two items")
		void size2RingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);
			ringBuffer.addFirst("item1");
			ringBuffer.addLast("item2");

			var result = ringBuffer.removeLast();

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(result).isEqualTo("item2");
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is empty")
		void emptyRingBuffer() {
			var ringBuffer = new RingBuffer<String>(3);

			assertThatThrownBy(ringBuffer::removeLast)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ring buffer is empty");
		}
	}
}
