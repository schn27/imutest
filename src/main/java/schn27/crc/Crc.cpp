#include "Crc.h"

namespace arinc825 {

template<> 
uint8_t Crc<uint8_t>::reverse(uint8_t x) const {
	return reverseByte(x);
}

template<> 
uint16_t Crc<uint16_t>::reverse(uint16_t x) const {
	x = ((x & 0xAAAA) >> 1) | ((x & 0x5555) << 1);
	x = ((x & 0xCCCC) >> 2) | ((x & 0x3333) << 2);
	x = ((x & 0xF0F0) >> 4) | ((x & 0x0F0F) << 4);
	return (x >> 8) | (x << 8);
}

template<>
uint32_t Crc<uint32_t>::reverse(uint32_t x) const {
	x = ((x & 0xAAAAAAAA) >> 1) | ((x & 0x55555555) << 1);
	x = ((x & 0xCCCCCCCC) >> 2) | ((x & 0x33333333) << 2);
	x = ((x & 0xF0F0F0F0) >> 4) | ((x & 0x0F0F0F0F) << 4);
	x = ((x & 0xFF00FF00) >> 8) | ((x & 0x00FF00FF) << 8);
	return (x >> 16) | (x << 16);
}

}