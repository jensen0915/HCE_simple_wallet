package hce_demo;

import java.util.Arrays;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
 
public class MyHostApduService extends HostApduService {
	static byte walletBalance = 0;

	private static final byte[] AID_SELECT_APDU = {
			(byte) 0x00, // CLA (class of command)
			(byte) 0xA4, // INS (instruction); A4 = select
			(byte) 0x04, // P1  (parameter 1)  (0x04: select by name)
			(byte) 0x00, // P2  (parameter 2)
			(byte) 0x07, // LC  (length of data)  
			(byte) 0xF0, (byte) 0x39, (byte) 0x41, (byte) 0x48, (byte) 0x14, (byte) 0x81, (byte) 0x00,
			(byte) 0x00 // LE   (max length of expected result, 0 implies 256)
	};
	
	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		String inboundApduDescription;
		byte[] responseApdu;

		if (Arrays.equals(AID_SELECT_APDU, apdu)) {
			inboundApduDescription = "Application selected";
			Log.i("HCEDEMO", inboundApduDescription);
			byte[] answer = new byte[2];
			answer[0] = (byte) 0x90;
			answer[1] = (byte) 0x00;
			responseApdu = answer;
			return responseApdu;
		}

		else if (selectAddMoneyApdu(apdu)) {
			Log.i("HCEDEMO", "ADD selected");
			int length = apdu[4];
			System.out.println("length = " + length);
			byte[] answer = new byte[3];
			
			walletBalance = (byte)(walletBalance + apdu[5]);
			answer[0] = (byte) 0x90;
			answer[1] = (byte) 0x00;			
			answer[2] = walletBalance;
			responseApdu = answer;
			return responseApdu;
		}
		
		else if (selectDebitApdu(apdu)) {
			Log.i("HCEDEMO", "Debit selected");
			int length = apdu[4];
			System.out.println("length = " + length);
			byte[] answer = new byte[3];
			
			// balance can not be negative
			if ( (byte)( (byte) walletBalance - apdu[5]) < (byte) 0 ) { 
				answer[0] = (byte) 0x01;
				answer[1] = (byte) 0x02;				
				responseApdu = answer;
				return responseApdu;
			}
			
			walletBalance = (byte)(walletBalance - apdu[5]);						
			answer[0] = (byte) 0x90;
			answer[1] = (byte) 0x00;			
			answer[2] = walletBalance;
			responseApdu = answer;
			return responseApdu;
		}
		
		else if (selectCheckBalanceApdu(apdu)) {
			Log.i("HCEDEMO", "check balance selected");
			byte[] answer = new byte[3];
			answer[0] = (byte) 0x90;
			answer[1] = (byte) 0x00;			
			answer[2] = walletBalance;
			responseApdu = answer;
			return responseApdu;
		}
				
		else {
			Log.i("HCEDEMO", "Unknown command");
			byte[] answer = new byte[2];
			answer[0] = (byte) 0x6F;
			answer[1] = (byte) 0x00;
			responseApdu = answer;
			return responseApdu;
		}
	}
	
	private boolean selectAddMoneyApdu(byte[] apdu) { 
//		(byte) 0x80,  // CLA
//		(byte) 0x01,  // INS
//		(byte) 0x00,  // P1
//		(byte) 0x00,  // P2				
		return apdu.length >= 2 && apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0x01 
		&& apdu[2] == (byte) 0x00 && apdu[3] == (byte) 0x00;
	}
	
	private boolean selectDebitApdu(byte[] apdu) { 
//		(byte) 0x80,  // CLA
//		(byte) 0x02,  // INS
//		(byte) 0x00,  // P1
//		(byte) 0x00,  // P2				
		return apdu.length >= 2 && apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0x02 
		&& apdu[2] == (byte) 0x00 && apdu[3] == (byte) 0x00;
	}
	
	private boolean selectCheckBalanceApdu(byte[] apdu) { 
//		(byte) 0x80,  // CLA
//		(byte) 0x03,  // INS
//		(byte) 0x00,  // P1
//		(byte) 0x00,  // P2				
		return apdu.length >= 2 && apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0x03 
		&& apdu[2] == (byte) 0x00 && apdu[3] == (byte) 0x00;
	}

	@Override
	public void onDeactivated(int reason) {
		Log.i("HCEDEMO", "Deactivated: " + reason);
	}
}