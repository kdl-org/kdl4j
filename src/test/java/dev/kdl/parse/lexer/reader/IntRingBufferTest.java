package dev.kdl.parse.lexer.reader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntRingBufferTest {
	@Nested
	@DisplayName("addFirst(T) should")
	class AddFirst {
		@Test
		@DisplayName("add an item to an empty ring buffer")
		void emptyRingBuffer() {
			var ringBuffer = new IntRingBuffer(3);

			ringBuffer.addFirst(1);

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(ringBuffer.get(0)).isEqualTo(1);
		}

		@Test
		@DisplayName("add an item to a ring buffer of size 1")
		void size1RingBuffer() {
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);

			ringBuffer.addFirst(2);

			assertThat(ringBuffer.size()).isEqualTo(2);
			assertThat(ringBuffer.get(0)).isEqualTo(2);
			assertThat(ringBuffer.get(1)).isEqualTo(1);
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is full")
		void fullRingBuffer() {
			var ringBuffer = new IntRingBuffer(1);
			ringBuffer.addFirst(1);

			assertThatThrownBy(() -> ringBuffer.addFirst(2))
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
			var ringBuffer = new IntRingBuffer(3);

			ringBuffer.addLast(1);

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(ringBuffer.get(0)).isEqualTo(1);
		}

		@Test
		@DisplayName("add an item to a ring buffer of size 1")
		void size1RingBuffer() {
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addLast(1);

			ringBuffer.addLast(2);

			assertThat(ringBuffer.size()).isEqualTo(2);
			assertThat(ringBuffer.get(0)).isEqualTo(1);
			assertThat(ringBuffer.get(1)).isEqualTo(2);
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is full")
		void fullRingBuffer() {
			var ringBuffer = new IntRingBuffer(1);
			ringBuffer.addLast(1);

			assertThatThrownBy(() -> ringBuffer.addLast(2))
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
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);

			var result = ringBuffer.get(0);

			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("throw an IndexOutOfBoundException when index is negative")
		void negativeIndex() {
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);

			assertThatThrownBy(() -> ringBuffer.get(-1))
				.isInstanceOf(IndexOutOfBoundsException.class)
				.hasMessage("Index -1 out of bounds for size 1");
		}

		@Test
		@DisplayName("throw an IndexOutOfBoundException when index is equal to the size")
		void outOfBoundsIndex() {
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);

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
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);

			var result = ringBuffer.removeFirst();

			assertThat(ringBuffer.size()).isEqualTo(0);
			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("remove and return the first item when the buffer has two items")
		void size2RingBuffer() {
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);
			ringBuffer.addLast(2);

			var result = ringBuffer.removeFirst();

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is empty")
		void emptyRingBuffer() {
			var ringBuffer = new IntRingBuffer(3);

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
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);

			var result = ringBuffer.removeLast();

			assertThat(ringBuffer.size()).isEqualTo(0);
			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("remove and return the first item when the buffer has two items")
		void size2RingBuffer() {
			var ringBuffer = new IntRingBuffer(3);
			ringBuffer.addFirst(1);
			ringBuffer.addLast(2);

			var result = ringBuffer.removeLast();

			assertThat(ringBuffer.size()).isEqualTo(1);
			assertThat(result).isEqualTo(2);
		}

		@Test
		@DisplayName("throw an IllegalStateException when the buffer is empty")
		void emptyRingBuffer() {
			var ringBuffer = new IntRingBuffer(3);

			assertThatThrownBy(ringBuffer::removeLast)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ring buffer is empty");
		}
	}
}
