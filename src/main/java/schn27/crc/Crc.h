#ifndef ARINC825_CRC_H
#define ARINC825_CRC_H

#include <stdint.h>
#include "NoCopy.h"

namespace arinc825 {

template <typename T>
class Crc : NoCopy {
public:
	Crc(T poly, T init, bool reverseInput, bool reverseOutput, T finalXor)
		: width_(sizeof(T) * 8)
		, poly_(poly)
		, init_(init)
		, reverseInput_(reverseInput)
		, reverseOutput_(reverseOutput)
		, finalXor_(finalXor)
		, crc_(init)
	{}

	void accumulate(uint8_t c) {
		c = reverseInput_ ? reverseByte(c) : c;
		crc_ ^= c << (width_ - 8);

		for (int i = 0; i < 8; ++i)
			crc_ = (crc_ & (1 << (width_ - 1))) ? (crc_ << 1) ^ poly_ : (crc_ << 1);
	}

	T get() const {
		return (reverseOutput_ ? reverse(crc_) : crc_) ^ finalXor_;
	}

	void reset() {
		crc_ = init_;
	}

private:
	Crc();
	
	uint8_t reverseByte(uint8_t x) const {
		x = ((x & 0xAA) >> 1) | ((x & 0x55) << 1);
		x = ((x & 0xCC) >> 2) | ((x & 0x33) << 2);
		return (x >> 4) | (x << 4);
	}

	T reverse(T x) const;

	const uint8_t width_;
	const T poly_;
	const T init_;
	const bool reverseInput_;
	const bool reverseOutput_;
	const T finalXor_;
	T crc_;
};



}

#endif
