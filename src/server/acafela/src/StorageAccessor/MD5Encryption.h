#pragma once
#include <array>
#include <iterator>
#include <cstdint>

class MD5 {
private:
	std::uint32_t a0_;
	std::uint32_t b0_;
	std::uint32_t c0_;
	std::uint32_t d0_;

	std::array<std::uint32_t, 16> m_array_;
	std::array<std::uint32_t, 16>::iterator m_array_first_;

	static const std::array<std::uint32_t, 64> k_array_;
	static const std::array<std::uint32_t, 64> s_array_;

private:
	static std::uint32_t left_rotate(std::uint32_t x, std::uint32_t c) {
		return (x << c) | (x >> (32 - c));
	}

	template <class OutputIterator>
	static void uint32_to_byte(std::uint32_t n, OutputIterator & first) {

		*first++ = n & 0xff;
		*first++ = (n >> 8) & 0xff;
		*first++ = (n >> 16) & 0xff;
		*first++ = (n >> 24) & 0xff;
	}

	template <class OutputIterator>
	static void uint32_to_hex(std::uint32_t n, OutputIterator & first) {
		const char * hex_chars = "0123456789abcdef";

		std::uint32_t b;

		b = n & 0xff;
		*first++ = hex_chars[b >> 4];
		*first++ = hex_chars[b & 0xf];

		b = (n >> 8) & 0xff;
		*first++ = hex_chars[b >> 4];
		*first++ = hex_chars[b & 0xf];

		b = (n >> 16) & 0xff;
		*first++ = hex_chars[b >> 4];
		*first++ = hex_chars[b & 0xf];

		b = (n >> 24) & 0xff;
		*first++ = hex_chars[b >> 4];
		*first++ = hex_chars[b & 0xf];
	}

private:
	void reset_m_array() {
		m_array_first_ = m_array_.begin();
	}

	template <class InputIterator>
	void bytes_to_m_array(InputIterator & first, std::array<std::uint32_t, 16>::iterator m_array_last) {
		for (; m_array_first_ != m_array_last; ++m_array_first_) {
			*m_array_first_ = *first++;
			*m_array_first_ |= *first++ << 8;
			*m_array_first_ |= *first++ << 16;
			*m_array_first_ |= *first++ << 24;
		}
	}

	template <class InputIterator>
	void true_bit_to_m_array(InputIterator & first, std::ptrdiff_t chunk_length) {
		switch (chunk_length % 4) {
		case 0:
			*m_array_first_++ = 0x00000080;
			break;
		case 1:
			*m_array_first_++ = *first++;
			*m_array_first_ |= 0x00008000;
			break;
		case 2:
			*m_array_first_++ = *first++;
			*m_array_first_ |= *first++ << 8;
			*m_array_first_ |= 0x00800000;
			break;
		case 3:
			*m_array_first_++ = *first++;
			*m_array_first_ |= *first++ << 8;
			*m_array_first_ |= *first++ << 16;
			*m_array_first_ |= 0x80000000;
			break;
		}
	}

	void zeros_to_m_array(std::array<std::uint32_t, 16>::iterator m_array_last) {
		for (; m_array_first_ != m_array_last; ++m_array_first_) {
			*m_array_first_ = 0;
		}
	}

	void original_length_bits_to_m_array(std::uint64_t original_length_bits) {
		original_length_bits &= 0xffffffffffffffff;
		*m_array_first_++ = (original_length_bits) & 0x00000000ffffffff;
		*m_array_first_++ = (original_length_bits & 0xffffffff00000000) >> 32;
	}

	void hash_chunk() {
		std::uint32_t A = a0_;
		std::uint32_t B = b0_;
		std::uint32_t C = c0_;
		std::uint32_t D = d0_;

		std::uint32_t F;
		unsigned int g;

		for (unsigned int i = 0; i < 64; ++i) {
			if (i < 16) {
				F = (B & C) | ((~B) & D);
				g = i;
			}
			else if (i < 32) {
				F = (D & B) | ((~D) & C);
				g = (5 * i + 1) & 0xf;
			}
			else if (i < 48) {
				F = B ^ C ^ D;
				g = (3 * i + 5) & 0xf;
			}
			else {
				F = C ^ (B | (~D));
				g = (7 * i) & 0xf;
			}

			std::uint32_t D_temp = D;
			D = C;
			C = B;
			B += left_rotate(A + F + k_array_[i] + m_array_[g], s_array_[i]);
			A = D_temp;
		}

		a0_ += A;
		b0_ += B;
		c0_ += C;
		d0_ += D;
	}

public:
	template <class InputIterator>
	void update(InputIterator first, InputIterator last) {

		std::uint64_t original_length_bits = std::distance(first, last) * 8;

		std::ptrdiff_t chunk_length;
		while ((chunk_length = std::distance(first, last)) >= 64) {
			reset_m_array();
			bytes_to_m_array(first, m_array_.end());
			hash_chunk();
		}

		reset_m_array();
		bytes_to_m_array(first, m_array_.begin() + chunk_length / 4);
		true_bit_to_m_array(first, chunk_length);

		if (chunk_length >= 56) {
			zeros_to_m_array(m_array_.end());
			hash_chunk();

			reset_m_array();
			zeros_to_m_array(m_array_.end() - 2);
			original_length_bits_to_m_array(original_length_bits);
			hash_chunk();
		}
		else {
			zeros_to_m_array(m_array_.end() - 2);
			original_length_bits_to_m_array(original_length_bits);
			hash_chunk();
		}
	}

public:
	MD5()
		: 
		a0_(0x67452301),
		b0_(0xefcdab89),
		c0_(0x98badcfe),
		d0_(0x10325476)
	{}

	template <class Container>
	void digest(Container & container) {
		container.resize(16);
		auto it = container.begin();

		uint32_to_byte(a0_, it);
		uint32_to_byte(b0_, it);
		uint32_to_byte(c0_, it);
		uint32_to_byte(d0_, it);
	}

	template <class Container>
	void hex_digest(Container & container) {
		container.resize(32);
		auto it = container.begin();

		uint32_to_hex(a0_, it);
		uint32_to_hex(b0_, it);
		uint32_to_hex(c0_, it);
		uint32_to_hex(d0_, it);
	}
};