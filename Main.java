package orb.ravenons.swbfresfix;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		
		// File input / output
		FileInputStream fis = new FileInputStream(args[0]);
		FileOutputStream fos = new FileOutputStream(args[1]);
		
		// File as an array of bytes (0x45C bytes is the size of a SWBT savegame)
		byte[] file = new byte[0x45C];
		
		// Read and close file
		fis.read(file);
		fis.close();
		
		// File structure
		// ---------------------------------------------
		// 0x0 - 0x3		--> Header (second checksum)
		// 0x4 - 0x457		--> Body (savegame contents)
		// 0x458 - 0x45B	--> Footer (first checksum)
		// ---------------------------------------------
		
		// Resolution fix begins here...
		
		// Get resolution from arguments
		int horizontalResolution = Integer.parseInt(args[2]);
		int verticalResolution = Integer.parseInt(args[3]);
		
		
		// Resolution in file (absolute offset, whole file)
		// ----------------------------------------------------------
		// 0x376 - 0x377 	--> Horizontal resolution (little-endian)
		// 0x378 - 0x379	--> Vertical resolution (little-endian)
		// ----------------------------------------------------------
		
		// Resolution stored as 2 byte little-endian each field
		byte firstByteHorizontal = (byte) horizontalResolution;
		byte secondByteHorizontal = (byte) (horizontalResolution >> 0x8);
		byte firstByteVertical = (byte) verticalResolution;
		byte secondByteVertical = (byte) (verticalResolution >> 0x8);
		
		// Change resolution in file
		file[0x376] = firstByteHorizontal;
		file[0x377] = secondByteHorizontal;
		file[0x378] = firstByteVertical;
		file[0x379] = secondByteVertical;
		
		// Resolution fix ends here...
		
		// Fix savegame (checksum...)
		fixSavegame(file);
		
		// Write and close file
		fos.write(file);
		fos.close();
	}
	
	public static void fixSavegame(byte[] file) throws IOException {
		
		// 0x0 - 0x3		--> Header (second checksum)
		// 0x4 - 0x457		--> Body (savegame contents)
		// 0x458 - 0x45B	--> Footer (first checksum)
		
		byte[] header = new byte[0x4];
		byte[] body = new byte[0x454];
		byte[] footer = new byte[0x4];
		
		System.arraycopy(file, 0x4, body, 0, 0x454);	// Copy body from file
		
		// Don't ask me why, but before the first checksum it sets that byte to 0x0 (it's 0x1 before setting)
		// After the first checksum and before the second checksum, that byte is restored to 0x1
		body[0x450] = 0x0;
		
		// First checksum
		footer = checksum(body, 0x454, 0);
		
		// Restores that byte to 0x1 before the second checksum
		body[0x450] = 0x1;
		
		// Body and footer together, to calculate the second checksum
		byte[] bodyAndFooter = new byte[0x458];
		System.arraycopy(body, 0, bodyAndFooter, 0, 0x454);
		System.arraycopy(footer, 0, bodyAndFooter, 0x454, 0x4);
		
		// Second checksum
		header = checksum(bodyAndFooter, 0x458, 0);
		
		// Copy header, body and footer to file
		System.arraycopy(header, 0, file, 0, 0x4);
		System.arraycopy(bodyAndFooter, 0, file, 0x4, 0x458);
	}
	
	// Calculates the checksum (Original arguments from the reverse-engineered function)
	public static byte[] checksum(byte[] data, int size, int seed) throws IOException {
		
		// Checksum routine, not really a mistery...
		int acumulator = ~seed;
		int currentByte;
		int temp;
		int[] lookupTable = getLookupTable();
		
		for (int i = 0; i < size; i++) {
			currentByte = data[i] & 0xFF;		// Stupid java signed byte...
			temp = acumulator;
			temp >>>= 0x18;		// To mimic hex argument for shr (>>>= is the best operator ever)
			temp ^= currentByte;		// Guaranteed to be byte-sized	
			currentByte = lookupTable[temp];	// Lookup table...
			acumulator <<= 0x8;
			acumulator ^= currentByte;
		}
		
		acumulator = ~acumulator;
		
		// Little-endian strikes again... (Shouldn't be here, though, just to make main method cleaner)
		return new byte[]{(byte) (acumulator), (byte) (acumulator >> 0x8), (byte) (acumulator >> 0x10), (byte) (acumulator >> 0x18)};
	}
	
	public static int[] getLookupTable() throws IOException {
		
		int size = 0x100;		// Size in dwords
		int[] lookupTable = new int[size];
		FileInputStream fis = new FileInputStream("lookupTable.bin");
		
		// lookupTable.bin was dumped from memory, that's why it's little-endian
		for (int i = 0; i < size; i++) {
			lookupTable[i] = fis.read();
			lookupTable[i] |= fis.read() << 0x8;
			lookupTable[i] |= fis.read() << 0x10;
			lookupTable[i] |= fis.read() << 0x18;
		}
		
		fis.close();
		
		return lookupTable;
	}
}
