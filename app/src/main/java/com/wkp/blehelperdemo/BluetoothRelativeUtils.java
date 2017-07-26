package com.wkp.blehelperdemo;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * 
 *@TODO 蓝牙相关的工具类
 *@author denghuan
 *@2016-8-6 @上午11:50:41
 */
public class BluetoothRelativeUtils {
	public static final byte STX = 0x7E;
	public static final byte ETX = 0x7F;
	public static final byte DLE = (byte) 0xE7;
	public static final byte X = 0x58;
	public static final byte Y = 0x59;
	public static final byte Z = 0x5A;

	static int CrcTable[] = {0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0,
			0x0280, 0xC241, 0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1,
			0xC481, 0x0440, 0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1,
			0xCE81, 0x0E40, 0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0,
			0x0880, 0xC841, 0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1,
			0xDA81, 0x1A40, 0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0,
			0x1C80, 0xDC41, 0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0,
			0x1680, 0xD641, 0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1,
			0xD081, 0x1040, 0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1,
			0xF281, 0x3240, 0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0,
			0x3480, 0xF441, 0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0,
			0x3E80, 0xFE41, 0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1,
			0xF881, 0x3840, 0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0,
			0x2A80, 0xEA41, 0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1,
			0xEC81, 0x2C40, 0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1,
			0xE681, 0x2640, 0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0,
			0x2080, 0xE041, 0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1,
			0xA281, 0x6240, 0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0,
			0x6480, 0xA441, 0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0,
			0x6E80, 0xAE41, 0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1,
			0xA881, 0x6840, 0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0,
			0x7A80, 0xBA41, 0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1,
			0xBC81, 0x7C40, 0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1,
			0xB681, 0x7640, 0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0,
			0x7080, 0xB041, 0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0,
			0x5280, 0x9241, 0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1,
			0x9481, 0x5440, 0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1,
			0x9E81, 0x5E40, 0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0,
			0x5880, 0x9841, 0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1,
			0x8A81, 0x4A40, 0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0,
			0x4C80, 0x8C41, 0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0,
			0x4680, 0x8641, 0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1,
			0x8081, 0x4040};

	static byte[] crc16_tab_h = {(byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
			(byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01,
			(byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1,
			(byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01,
			(byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0,
			(byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
			(byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1,
			(byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80,
			(byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01,
			(byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1,
			(byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1,
			(byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80,
			(byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01,
			(byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1,
			(byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
			(byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
			(byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80,
			(byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1,
			(byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80,
			(byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
			(byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
			(byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
			(byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00,
			(byte) 0xC1, (byte) 0x81, (byte) 0x40};

	static byte[] crc16_tab_l = {(byte) 0x00, (byte) 0xC0, (byte) 0xC1,
			(byte) 0x01, (byte) 0xC3, (byte) 0x03, (byte) 0x02, (byte) 0xC2,
			(byte) 0xC6, (byte) 0x06, (byte) 0x07, (byte) 0xC7, (byte) 0x05,
			(byte) 0xC5, (byte) 0xC4, (byte) 0x04, (byte) 0xCC, (byte) 0x0C,
			(byte) 0x0D, (byte) 0xCD, (byte) 0x0F, (byte) 0xCF, (byte) 0xCE,
			(byte) 0x0E, (byte) 0x0A, (byte) 0xCA, (byte) 0xCB, (byte) 0x0B,
			(byte) 0xC9, (byte) 0x09, (byte) 0x08, (byte) 0xC8, (byte) 0xD8,
			(byte) 0x18, (byte) 0x19, (byte) 0xD9, (byte) 0x1B, (byte) 0xDB,
			(byte) 0xDA, (byte) 0x1A, (byte) 0x1E, (byte) 0xDE, (byte) 0xDF,
			(byte) 0x1F, (byte) 0xDD, (byte) 0x1D, (byte) 0x1C, (byte) 0xDC,
			(byte) 0x14, (byte) 0xD4, (byte) 0xD5, (byte) 0x15, (byte) 0xD7,
			(byte) 0x17, (byte) 0x16, (byte) 0xD6, (byte) 0xD2, (byte) 0x12,
			(byte) 0x13, (byte) 0xD3, (byte) 0x11, (byte) 0xD1, (byte) 0xD0,
			(byte) 0x10, (byte) 0xF0, (byte) 0x30, (byte) 0x31, (byte) 0xF1,
			(byte) 0x33, (byte) 0xF3, (byte) 0xF2, (byte) 0x32, (byte) 0x36,
			(byte) 0xF6, (byte) 0xF7, (byte) 0x37, (byte) 0xF5, (byte) 0x35,
			(byte) 0x34, (byte) 0xF4, (byte) 0x3C, (byte) 0xFC, (byte) 0xFD,
			(byte) 0x3D, (byte) 0xFF, (byte) 0x3F, (byte) 0x3E, (byte) 0xFE,
			(byte) 0xFA, (byte) 0x3A, (byte) 0x3B, (byte) 0xFB, (byte) 0x39,
			(byte) 0xF9, (byte) 0xF8, (byte) 0x38, (byte) 0x28, (byte) 0xE8,
			(byte) 0xE9, (byte) 0x29, (byte) 0xEB, (byte) 0x2B, (byte) 0x2A,
			(byte) 0xEA, (byte) 0xEE, (byte) 0x2E, (byte) 0x2F, (byte) 0xEF,
			(byte) 0x2D, (byte) 0xED, (byte) 0xEC, (byte) 0x2C, (byte) 0xE4,
			(byte) 0x24, (byte) 0x25, (byte) 0xE5, (byte) 0x27, (byte) 0xE7,
			(byte) 0xE6, (byte) 0x26, (byte) 0x22, (byte) 0xE2, (byte) 0xE3,
			(byte) 0x23, (byte) 0xE1, (byte) 0x21, (byte) 0x20, (byte) 0xE0,
			(byte) 0xA0, (byte) 0x60, (byte) 0x61, (byte) 0xA1, (byte) 0x63,
			(byte) 0xA3, (byte) 0xA2, (byte) 0x62, (byte) 0x66, (byte) 0xA6,
			(byte) 0xA7, (byte) 0x67, (byte) 0xA5, (byte) 0x65, (byte) 0x64,
			(byte) 0xA4, (byte) 0x6C, (byte) 0xAC, (byte) 0xAD, (byte) 0x6D,
			(byte) 0xAF, (byte) 0x6F, (byte) 0x6E, (byte) 0xAE, (byte) 0xAA,
			(byte) 0x6A, (byte) 0x6B, (byte) 0xAB, (byte) 0x69, (byte) 0xA9,
			(byte) 0xA8, (byte) 0x68, (byte) 0x78, (byte) 0xB8, (byte) 0xB9,
			(byte) 0x79, (byte) 0xBB, (byte) 0x7B, (byte) 0x7A, (byte) 0xBA,
			(byte) 0xBE, (byte) 0x7E, (byte) 0x7F, (byte) 0xBF, (byte) 0x7D,
			(byte) 0xBD, (byte) 0xBC, (byte) 0x7C, (byte) 0xB4, (byte) 0x74,
			(byte) 0x75, (byte) 0xB5, (byte) 0x77, (byte) 0xB7, (byte) 0xB6,
			(byte) 0x76, (byte) 0x72, (byte) 0xB2, (byte) 0xB3, (byte) 0x73,
			(byte) 0xB1, (byte) 0x71, (byte) 0x70, (byte) 0xB0, (byte) 0x50,
			(byte) 0x90, (byte) 0x91, (byte) 0x51, (byte) 0x93, (byte) 0x53,
			(byte) 0x52, (byte) 0x92, (byte) 0x96, (byte) 0x56, (byte) 0x57,
			(byte) 0x97, (byte) 0x55, (byte) 0x95, (byte) 0x94, (byte) 0x54,
			(byte) 0x9C, (byte) 0x5C, (byte) 0x5D, (byte) 0x9D, (byte) 0x5F,
			(byte) 0x9F, (byte) 0x9E, (byte) 0x5E, (byte) 0x5A, (byte) 0x9A,
			(byte) 0x9B, (byte) 0x5B, (byte) 0x99, (byte) 0x59, (byte) 0x58,
			(byte) 0x98, (byte) 0x88, (byte) 0x48, (byte) 0x49, (byte) 0x89,
			(byte) 0x4B, (byte) 0x8B, (byte) 0x8A, (byte) 0x4A, (byte) 0x4E,
			(byte) 0x8E, (byte) 0x8F, (byte) 0x4F, (byte) 0x8D, (byte) 0x4D,
			(byte) 0x4C, (byte) 0x8C, (byte) 0x44, (byte) 0x84, (byte) 0x85,
			(byte) 0x45, (byte) 0x87, (byte) 0x47, (byte) 0x46, (byte) 0x86,
			(byte) 0x82, (byte) 0x42, (byte) 0x43, (byte) 0x83, (byte) 0x41,
			(byte) 0x81, (byte) 0x80, (byte) 0x40};

	public static int getCRC16(byte[] p) {
		if (p.length < 2) {
			return -1;
		}
		byte nCrc16Rec = p[p.length - 1];

		int result = ((nCrc16Rec << 8) | p[p.length - 2]);
		if (result < 0) {
			return 65536 + result;
		}
		return result;
	}

	/**
	 * CalcCRC161
	 *
	 * @param data
	 * @param len
	 * @param preval
	 * @return
	 */
	public static int CalcCRC161(byte[] data, int len, int preval) {
		int ucCRCHi = (preval & 0xff00) >> 8;
		int ucCRCLo = preval & 0x00ff;
		int iIndex;
		for (int i = 0; i < len; ++i) {
			iIndex = (ucCRCLo ^ data[0 + i]) & 0x00ff;
			ucCRCLo = ucCRCHi ^ crc16_tab_h[iIndex];
			ucCRCHi = crc16_tab_l[iIndex];
		}
		return ((ucCRCHi & 0x00ff) << 8) | (ucCRCLo & 0x00ff) & 0xffff;
	}

	/**
	 * 此函数在接收时调用
	 *
	 * @param inBuf
	 * @return
	 */
	public static byte[] PackInProcess(byte[] inBuf) {
		int nOutLen;
		int nOffset = 0;
		int nI, blSkip = 0;
		byte[] outBuf = new byte[256];
		if (inBuf.length < 3) {
//			Log.e("PackInProcess()", "字节长度不足:" + new String(inBuf));
			return inBuf;
		}
		int a = inBuf.length;
		// 对报文进行转义处理,对接收buffer的后3个字节（ETX,两位校验和）不做处理,
		for (nI = 1; nI < inBuf.length - 3; nI++) {
			// 如果前一个字符为DLE时，忽略下一个字符
			if (blSkip != 0) {
				blSkip = 0;
				continue;
			}
			// 如果是转义字符，则下一个为数据，将DLE去掉
			if (inBuf[nI] == DLE) {
				switch (inBuf[nI + 1]) {
					case X: {
						outBuf[nOffset] = STX;
						nOffset++;
						blSkip = 1;
						break;
					}
					case Y: {
						outBuf[nOffset] = ETX;
						nOffset++;
						blSkip = 1;
						break;
					}

					case Z: {
						outBuf[nOffset] = (byte) (DLE - 256);
						nOffset++;
						blSkip = 1;
						break;
					}
				}
			} else {
				outBuf[nOffset] = inBuf[nI];
				nOffset++;
			}
		}
		outBuf = Arrays.copyOfRange(outBuf, 0, nOffset);
		return outBuf;
	}

	/**
	 * //此函数在发送时调用
	 *
	 * @param inBuf
	 * @return
	 */
	public static byte[] PackOutProcess(byte[] inBuf) {
		int nI, nOffset;
		int nCrc16, nCrc16H, crcout;
		byte[] outBuf = new byte[256];
		outBuf[0] = STX; // 报文头
		nOffset = 1;
		for (nI = 0; nI < inBuf.length; nI++) {
			switch (inBuf[nI]) {
				case STX: // 如果报文中有STX字符，将STX替换成X字符，并在X之前加入转义字符DLE
				{
					outBuf[nOffset] = (byte) (DLE - 256);
					nOffset++;
					outBuf[nOffset] = X;
					nOffset++;
					break;
				}
				case ETX: // 如果报文中有ETX字符，将ETX替换成Y字符，并在Y之前加入转义字符DLE
				{
					outBuf[nOffset] = (byte) (DLE - 256);
					nOffset++;
					outBuf[nOffset] = Y;
					nOffset++;
					break;
				}
				case (byte) (DLE - 256): // 如果报文中有DLE字符，将DLE替换成Z字符，并在Z之前加入转义字符DLE
				{
					outBuf[nOffset] = (byte) (DLE - 256);
					nOffset++;
					outBuf[nOffset] = Z;
					nOffset++;
					break;
				}
				default: {
					outBuf[nOffset] = inBuf[nI];
					nOffset++;
					break;
				}
			}
		}
		// 写报文尾
		outBuf[nOffset] = ETX;
		nOffset++;

		// 计算校验和，包括报头报尾
		crcout = CalcCRC161(outBuf, nOffset, 0xffff); // 在接收和发送端的初值必须一样

		// 将校验和分成高低位送入发送buffer
		int aa = crcout & 0xff;
		if (aa > 127) {
			outBuf[nOffset] = (byte) (aa - 256);
		} else {
			outBuf[nOffset] = (byte) (aa);
		}
		nOffset++;

		int bb = ((crcout & 0xff00) >> 8);
		if (bb > 127) {
			outBuf[nOffset] = (byte) (bb - 256);
		} else {
			outBuf[nOffset] = (byte) (bb);
		}
		nOffset++;
		// 转换完毕

		outBuf = Arrays.copyOfRange(outBuf, 0, nOffset);
		return outBuf; // 返回转换后的数据长度（原始数据长度）
	}

	/**
	 * 2.6 设置蓝牙模块名称（客户端发送）
	 *
	 * @param deviceName 蓝牙模块名称
	 * @return
	 */
	public static byte[] getChangeDeviceNameByte(String deviceName) {

		byte[] newName = null;
		try {
			newName = deviceName.getBytes("utf-8");
			byte[] result = new byte[newName.length + 2];
			result[0] = 0x6;
			result[1] = (byte) newName.length;
			for (int i = 2; i < result.length; i++) {
				result[i] = newName[i - 2];
			}
			// System.arraycopy(newName, 0, result, 2, newName.length);
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 发送前合并多条信息
	 *
	 * @param bytes
	 * @return 合并后的整条信息
	 */
	public static byte[] preSend(byte flag, byte[][] bytes) {
		int length = 1;
		for (int i = 0; i < bytes.length; i++) {
			length += bytes[i].length;
		}
		byte[] results = new byte[length];
		results[0] = flag;
		int sum = 1;
		for (int i = 0; i < bytes.length; i++) {
			for (int j = 0; j < bytes[i].length; j++) {
				results[sum + j] = bytes[i][j];
			}
			sum += bytes[i].length;
		}
		return results;
	}

	/**
	 * 发送前合并多条信息
	 *
	 * @param bytes
	 * @return 合并后的整条信息
	 */
	public static byte[] preSend(byte flag, List<byte[]> bytes) {
		int length = 1;
		for (int i = 0; i < bytes.size(); i++) {
			length += bytes.get(i).length;
		}
		byte[] results = new byte[length];
		results[0] = flag;
		int sum = 1;
		for (int i = 0; i < bytes.size(); i++) {
			for (int j = 0; j < bytes.get(i).length; j++) {
				results[sum + j] = bytes.get(i)[j];
			}
			sum += bytes.get(i).length;
		}
		return results;
	}

	/**
	 * 给待发送的加密信息转义并分包处理
	 *
	 * @param bytes
	 * @return 转义分包后的加密信息
	 */
	public static byte[][] sendEncryptedMsg(byte[] bytes) {
		byte[] outProcess = PackOutProcess(bytes);
		int length = (outProcess.length / 20) + 1;
		byte[][] results = new byte[length][];
		if (outProcess.length > 20) {
			for (int i = 0; i < length; i++) {
				byte[] temps = null;
				if (outProcess.length - 20 * i > 20) {
					temps = new byte[20];
					for (int j = 20 * i, k = 0; j < 20 * (i + 1) && k < 20; j++, k++) {
						temps[k] = outProcess[j];
					}
				} else {
					temps = new byte[outProcess.length - 20 * i];
					for (int j = 20 * i, k = 0; j < outProcess.length && k < 20; j++, k++) {
						temps[k] = outProcess[j];
					}
				}
				results[i] = temps;
			}
		} else {
			results[0] = outProcess;
		}

		return results;
	}

	/**
	 * 给接收到的信息并包并转义处理
	 *
	 * @param bytes
	 * @return 并包转义后的加密信息
	 */
	public static byte[] receiveEncryptedMsg(byte[][] bytes) {
		int length = 0;
		for (int i = 0; i < bytes.length; i++) {
			length += bytes[i].length;
		}
		byte[] results = new byte[length];
		int sum = 0;
		for (int i = 0; i < bytes.length; i++) {
			for (int j = 0; j < bytes[i].length; j++) {
				results[sum + j] = bytes[i][j];
			}
			sum += bytes[i].length;
		}
		return PackInProcess(results);
	}

	/**
	 * 给接收到的信息并包并转义处理
	 *
	 * @param bytes
	 * @return 并包转义后的加密信息
	 */
	public static byte[] receiveEncryptedMsg(List<byte[]> bytes) {
		int length = 0;
		for (int i = 0; i < bytes.size(); i++) {
			length += bytes.get(i).length;
		}
		byte[] results = new byte[length];
		int sum = 0;
		for (int i = 0; i < bytes.size(); i++) {
			for (int j = 0; j < bytes.get(i).length; j++) {
				results[sum + j] = bytes.get(i)[j];
			}
			sum += bytes.get(i).length;
		}
		return PackInProcess(results);
	}

	/**
	 * 接收到消息后分离动态秘钥和密文口令
	 *
	 * @param bytes
	 * @return 秘钥和密文口令的二维字节数组
	 */
	public static byte[][] nextReceive(byte[] bytes) {
		if (bytes[0] != 0x9) {
			return null;
		}
		byte[][] results = new byte[2][];
		results[0] = new byte[8];
		results[1] = new byte[16];
		for (int i = 1; i < bytes.length; i++) {
			if (i < 9) {
				results[0][i - 1] = bytes[i];
			} else {
				results[1][i - 9] = bytes[i];
			}
		}

		return results;
	}

	/**
	 * 2.3 发送ID号（客户端发送）
	 *
	 * @param id ID号
	 * @return
	 */
	@SuppressWarnings("null")
	public static byte[] sendCardID(byte[] id) {
		byte[] result = new byte[5];
		result[0] = 0x3;
		for (int i = 1; i < result.length; i++) {
			result[i] = id[i - 1];
		}
		return result;
	}

	/**
	 * nfc发送ID号（客户端发送）
	 *
	 * @param id ID号
	 * @return
	 */
	@SuppressWarnings("null")
	public static byte[] nfcsendCardID(byte[] id) {

		return id;
	}

	public static String byteToHex(byte[] bytes) {
		String str = "";

		for (int i = 0; i < bytes.length; i++) {
			String s = "";
			if (bytes[i] < 0) {
				s = Integer.toHexString(bytes[i]).substring(6, 8);
			} else {
				s = Integer.toHexString(bytes[i]);
			}
			if (s.length() == 1) {
				s = "0" + s;
			}
			str += s;
		}

		return str.toUpperCase();

	}

	/**
	 * 发送指令
	 *
	 * @param preFlag
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static byte[] sendMsg(byte preFlag, byte[] content) throws Exception {
		byte[] key_8bytes = AESUtils.generate8Bytes();
		byte[] randomKey = AESUtils.generateRandomKey(key_8bytes);
		byte[] password = AESUtils.encrypt(content, randomKey);
		byte[][] sendMsg = new byte[2][];
		sendMsg[0] = key_8bytes;
		sendMsg[1] = password;
		return PackOutProcess(preSend(preFlag, sendMsg));
	}


	/**
	 * 从十六进制字符串到字节数组转换
	 *
	 * @param hexstr 十六进制字符串
	 * @return
	 */

	public static byte[] HexString2Bytes(String hexstr) {
		byte[] b = new byte[hexstr.length() / 2];
		int j = 0;
		for (int i = 0; i < b.length; i++) {
			char c0 = hexstr.charAt(j++);
			char c1 = hexstr.charAt(j++);
			b[i] = (byte) ((parse(c0) << 4) | parse(c1));
		}
		return b;
	}

	private static int parse(char c) {
		if (c >= 'a')
			return (c - 'a' + 10) & 0x0f;
		if (c >= 'A')
			return (c - 'A' + 10) & 0x0f;
		return (c - '0') & 0x0f;
	}
}
