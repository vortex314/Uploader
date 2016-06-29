package be.limero.programmer;

/*
3.1 Get command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
3.2 Get Version & Read Protection Status command . . . . . . . . . . . . . . . . . . . 10
3.3 Get ID command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .11
3.4 Read Memory command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
3.5 Go command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
3.6 Write Memory command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
3.7 Erase Memory command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
3.8 Extended Erase Memory command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
3.9 Write Protect command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
3.10 Write Unprotect command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
3.11 Readout Protect command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
3.12 Readout Unprotect command . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 33
*/

public class Stm32Protocol {

	static final byte X_WAIT_ACK = 0x40; // wait for ACK
	static final byte X_SEND = 0x41; // send fixed size length
	static final byte X_RECV = 0x42; // receive fixed size length
	static final byte X_RECV_VAR = 0x43; // receive first byte for length of
											// rest
											// to receive
	static final byte X_RECV_VAR_MIN_1 = 0x44; // receive first byte for length
												// of
												// rest
	// to receive

	static final byte GET = 0;
	static final byte GET_VERSION = 1;
	static final byte GET_ID = 2;
	static final byte READ_MEMORY = 0x11;
	static final byte GO = 0x21;
	static final byte WRITE_MEMORY = 0x31;
	static final byte ERASE_MEMORY = 0x41;
	static final byte EXTENDED_ERASE_MEMORY = 0x44;
	static final byte WRITE_PROTECT = 0x63;
	static final byte WRITE_UNPROTECT = 0x73;
	static final byte READ_PROTECT = (byte) 0x82;
	static final byte READ_UNPROTECT = (byte) 0x92;

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 3];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 3] = hexArray[v >>> 4];
			hexChars[j * 3 + 1] = hexArray[v & 0x0F];
			hexChars[j * 3 + 2] = ' ';
		}
		return new String(hexChars);
	}

	static void ASSERT(boolean expr) throws ArrayIndexOutOfBoundsException {
		if (expr)
			return;
		throw new ArrayIndexOutOfBoundsException(" ASSERTION FAILED ");
	}

	static byte xor(int a) {
		return (byte) (a ^ (-1) & 0xFF);
	}

	static byte slice(int word, int offset) {
		return (byte) ((word >> (offset * 8)) & 0xFF);
	}

	static byte fullXor(int word) {
		return (byte) (slice(word, 0) ^ slice(word, 1) ^ slice(word, 2) ^ slice(word, 3));
	}

	static byte fullXor(byte[] arr) {
		byte x = arr[0];
		for (int i = 1; i < arr.length; i++)
			x ^= arr[i];
		return x;
	}

	static byte[] Get() {
		return new byte[] { X_SEND, 2, GET, xor(GET), X_WAIT_ACK, X_RECV_VAR, X_WAIT_ACK };
	}

	static byte[] GetVersion() {
		return new byte[] { X_SEND, 2, GET_VERSION, xor(GET_VERSION), X_WAIT_ACK, X_RECV, 3, X_WAIT_ACK };
	}

	static byte[] GetId() {
		return new byte[] { X_SEND, 2, GET_ID, xor(GET_ID), X_WAIT_ACK, X_RECV_VAR_MIN_1, X_RECV, 2, X_WAIT_ACK };
	}

	static byte[] ReadMemory(int address, int length) {
		ASSERT(length > 0 && length < 257);
		byte Read_Memory[] = { X_SEND, 2, READ_MEMORY, xor(READ_MEMORY), X_WAIT_ACK, //
				X_SEND, 5, slice(address, 3), slice(address, 2), //
				slice(address, 1), slice(address, 0), fullXor(address), X_WAIT_ACK, //
				X_SEND, 2, (byte) (length - 1), xor(length - 1), X_WAIT_ACK, //
				X_RECV, (byte) length, X_WAIT_ACK };
		return Read_Memory;
	}

	static byte[] Go(int address) {
		return new byte[] { X_SEND, 2, GO, xor(GO), X_WAIT_ACK, X_SEND, 5, slice(address, 3), slice(address, 2), //
				slice(address, 1), slice(address, 0), fullXor(address), X_WAIT_ACK };
	}

	static byte[] WriteMemory(int address, byte[] instr) {
		byte[] Write_Memory = { X_SEND, 2, WRITE_MEMORY, xor(WRITE_MEMORY), X_WAIT_ACK, X_SEND, 5, slice(address, 3),
				slice(address, 2), //
				slice(address, 1), slice(address, 0), fullXor(address), X_WAIT_ACK };
		byte[] Write_Memory_Closure = { fullXor(instr), X_WAIT_ACK };
		byte[] result = new byte[instr.length + Write_Memory.length + Write_Memory_Closure.length];
		System.arraycopy(Write_Memory, 0, result, 0, Write_Memory.length);
		System.arraycopy(instr, 0, result, Write_Memory.length, instr.length);
		System.arraycopy(Write_Memory_Closure, 0, result, Write_Memory.length + instr.length,
				Write_Memory_Closure.length);
		return result;
	}

	static byte[] GlobalEraseMemory() {
		byte[] Global_Erase_Memory = { X_SEND, 2, ERASE_MEMORY, xor(ERASE_MEMORY), X_WAIT_ACK, 0, xor(0), X_WAIT_ACK };
		return Global_Erase_Memory;
	}

	static byte[] EraseMemory(byte[] pages) {
		byte[] Erase_Memory = { X_SEND, 2, ERASE_MEMORY, xor(ERASE_MEMORY), X_WAIT_ACK, (byte) (pages.length - 1) };
		byte[] result = new byte[Erase_Memory.length + pages.length];
		System.arraycopy(Erase_Memory, 0, result, 0, Erase_Memory.length);
		System.arraycopy(pages, 0, result, Erase_Memory.length, pages.length);
		// TODO add length in checksum
		result[Erase_Memory.length + pages.length] = fullXor(pages);
		result[Erase_Memory.length + pages.length + 1] = X_WAIT_ACK;
		return result;
	}

	static void add(byte[] arr, int offset, int value) {
		arr[offset] = slice(value, 1);
		arr[offset + 1] = slice(value, 0);
	}

	static byte crc(byte[] arr, int offset, int length) {
		byte x = arr[offset];
		for (int i = 1; i < length; i++)
			x ^= arr[offset + i];
		return x;
	}

	static byte[] ExtendedEraseMemory(int[] pages) {
		byte[] Extended_Erase_Memory = { X_SEND, 2, EXTENDED_ERASE_MEMORY, xor(EXTENDED_ERASE_MEMORY), X_WAIT_ACK,
				X_SEND, (byte) (pages.length - 1) };
		byte[] result = new byte[Extended_Erase_Memory.length + pages.length * 2 + 4];
		System.arraycopy(Extended_Erase_Memory, 0, result, 0, Extended_Erase_Memory.length);
		int offset = Extended_Erase_Memory.length;
		byte crc;
		crc = result[offset++] = slice(pages.length - 1, 1);
		crc ^= result[offset++] = slice(pages.length - 1, 0);
		for (int i = 0; i < pages.length; i++) {
			crc ^= result[offset++] = slice(pages[i], 1);
			crc ^= result[offset++] = slice(pages[i], 0);
		}
		result[offset++] = crc;
		result[offset++] = X_WAIT_ACK;
		return result;
	}

	static byte[] WriteProtect(byte[] sectors) {
		byte[] WriteProtect = { X_SEND, 2, WRITE_PROTECT, xor(WRITE_PROTECT), X_WAIT_ACK, X_SEND,
				(byte) (sectors.length - 1) };
		byte crc = (byte) (sectors.length - 1);
		byte[] result = new byte[WriteProtect.length + sectors.length * 2 + 4];
		System.arraycopy(WriteProtect, 0, result, 0, WriteProtect.length);
		int offset = WriteProtect.length;
		for (int i = 0; i < sectors.length; i++) {
			crc ^= result[offset++] = sectors[i];
		}
		result[offset++] = crc;
		result[offset++] = X_WAIT_ACK;
		return result;
	}

	static byte[] WriteUnprotect() {
		byte[] WriteUnprotect = { X_SEND, 2, WRITE_UNPROTECT, xor(WRITE_UNPROTECT), X_WAIT_ACK, X_WAIT_ACK };
		return WriteUnprotect;
	}

	static byte[] ReadProtect() {
		byte[] ReadProtect = { X_SEND, 2, READ_PROTECT, xor(READ_PROTECT), X_WAIT_ACK, X_WAIT_ACK };
		return ReadProtect;
	}

	static byte[] ReadUnprotect() {
		byte[] ReadUnprotect = { X_SEND, 2, READ_UNPROTECT, xor(READ_UNPROTECT), X_WAIT_ACK, X_WAIT_ACK };
		return ReadUnprotect;
	}

	static int globalId = 1;

	static int newId() {
		return globalId++ & 0xFFFF;
	}

	class Request {
		/*
		 * enum ReqKey { FLASH_MODE, RUN_MODE, FLASH_INPUT, RUN_INPUT };
		 */

		int id;
		byte[] input;
		String key;
		String value;

		Request(byte[] data) {
			id=newId();
			input=data;
		}
	}

	class Reply {
		int id;
		int errno;
		byte[] output;
	}

	public static void main(String[] args) {
		System.out.println(bytesToHex(Stm32Protocol.Get()));
		System.out.println(bytesToHex(ReadMemory(0xFF00FF00, 256)));
		System.out.println(bytesToHex(WriteMemory(0xA1A2A3A4, new byte[] { 1, 2, 3, 4, 5, 7, 11, 13, 15 })));
		System.out.println(bytesToHex(ExtendedEraseMemory(new int[] { 1, 2 })));
	}

}
