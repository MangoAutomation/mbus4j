package net.sf.mbus4j.master.devices;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import de.ibapl.spsw.provider.SerialPortSocketFactoryImpl;
import net.sf.mbus4j.dataframes.DeviceId;
import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.dataframes.SendInitSlave;
import net.sf.mbus4j.dataframes.SendUserDataFrame;
import net.sf.mbus4j.dataframes.UserDataResponse;
import net.sf.mbus4j.master.MBusMaster;
import org.junit.Ignore;

@Ignore
public class Test_SBC_Electricity_0x24 {

	MBusMaster master;
	DeviceId deviceId;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		master = new MBusMaster();
		SerialPortSocket serialPortSocket = SerialPortSocketFactoryImpl.singleton().createSerialPortSocket("/dev/ttyUSB0");
//		SerialPortSocket serialPortSocket = new Ser2NetProvider("localhost", 4001);
		master.setSerialPort(LoggingSerialPortSocket.wrapWithHexOutputStream(serialPortSocket, new FileOutputStream("/tmp/test_SBC.txt", false),  false, TimeStampLogging.FROM_OPEN));
		master.setResponseTimeoutOffset(0);
		master.open();
		deviceId = new DeviceId((byte)0x24, MBusMedium.ELECTRICITY, 00110011, "SBC", (byte)0x03);
	}

	@After
	public void tearDown() throws Exception {
		master.close();
	}

	@Test
	public void testSND_NKE() throws IOException {
		SendInitSlave SND_NKE = new SendInitSlave(deviceId.address);
		master.send(SND_NKE, 1);
		master.send(SND_NKE, 1);
		master.send(SND_NKE, 1);
		master.send(SND_NKE, 1);
		master.send(SND_NKE, 1);
	}
	
	@Test
	public void testREQ_UDR2_100_Times() throws IOException {
		for (int i = 0; i < 100; i++) {
			UserDataResponse udr = master.sendRequestUserData(SendUserDataFrame.DEFAULT_FCB, SendUserDataFrame.DEFAULT_FCB, deviceId.address);
			assertEquals("udr_0 DataBlockCount: " + udr.getDataBlockCount() + "\n" + udr,  6, udr.getDataBlockCount());
			assertTrue("udr_0" + udr,  udr.isLastPackage());
			System.err.println("INDEX: " + i + ">\n" + udr + "<INDEX " + i);
			assertTrue("UDR:" + udr, udr.isLastPackage());
		}
	}

}
