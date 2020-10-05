package com.sv2x.googlemap3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class User {
	User() {
		userId = "";
		userName = "";
		ownSpaceId = false;
		spaceId = "-1";
		addr = null;
		port = 0;
		connectionActive = true;
		mLastUpdateTime = "0";
	}

	String userId;          // unique user id
	String userName;         // additional user name
	InetAddress addr;        // ipv4 addr
	int port;              // port number
	boolean connectionActive;  // connectiivty, 1 = active, 0 = inactive
	public boolean ownSpaceId; // a owner of a space?
	String spaceId;             // space where the user belongs to
	String mLatitude;
	String mLongitude;
	String mLastUpdateTime;

	void send(final DatagramSocket skt, String msg ) {
		if(addr == null || port == 0) return;
		final DatagramPacket pkt;
		pkt = new DatagramPacket(msg.getBytes(), msg.length(), addr, port);
		new Thread(new Runnable() {
			public void run() {
				try {
					skt.send(pkt);
				} catch (IOException e) {
					e.printStackTrace( );
				}
			}
		}).start();
	}
};

class Users {
	List<User> userList;
	Users() {
		userList = new ArrayList<User>();
	}
	void addUser(User u) {
		userList.add(u);
	}
	void removeUser(User u) {
		userList.remove(u);
	}
	public User findUser(String id) {
		for(User u : userList) {
			if(u.userId.equals(id)) {
				return u;
			}
		}
		return null;
	}
};

class Receive implements Runnable {
	static final String NEW_USER = "New User";
	boolean keyup = false;
	DatagramSocket rSocket = null;
	DatagramPacket rPacket = null;
	Users uList;
	byte[] rMessage = new byte[2000];

	public Receive(DatagramSocket sck, Users u) {
		this.rSocket = sck;
		this.uList = u;
	}

	public void run() {
		while (true) {
			rPacket = new DatagramPacket(rMessage, rMessage.length);
			try {
				rSocket.receive(rPacket);
				handlePacket(rPacket);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void handlePacket(DatagramPacket pkt) {
		String msg;
		int index;
		User clientUser;
		String clientId;
		String clientName;
		String spaceId;
		String msgType;
		String requestedSpaceId;
		User tempUser;

		msg = new String(rMessage, 0, pkt.getLength());
		index = msg.indexOf(";");
		if (index <= 0) {
			System.out.println("Unknown message");
			return;
		} else {
			clientId = msg.substring(0, index);
			msg = msg.substring(index + 1, msg.length());
		}

		index = msg.indexOf(";");
		if (index <= 0) {
			System.out.println("Unknown message type");
			return;
		} else {
			msgType = msg.substring(0, index);
			msg = msg.substring(index + 1, msg.length());
		}

		if (msgType.equals(NEW_USER)) {
			addUser(clientId, pkt);
		} else {
			System.out.println("Unknown message type");
		}
	}

	void addUser(String clientId, DatagramPacket pkt) {
		User user;
		user = uList.findUser(clientId);
		if (user != null) {
			System.out.println("User exists");
			user.addr = pkt.getAddress();
			user.port = pkt.getPort();
		} else {
			user = new User();
			user.userId = clientId;
			user.addr = pkt.getAddress();
			user.port = pkt.getPort();
			user.connectionActive = true;
			uList.addUser(user);
		}

		CharSequence response;
		response = NEW_USER + " OK;" + user.userId;
		user.send(rSocket, response.toString());

		KeyDetect keyDetect = new KeyDetect(user, rSocket);

		Thread t = new Thread(keyDetect);
		t.start();
	}
}

class KeyDetect implements Runnable
{
	User nxtBot;
	DatagramSocket nxtSocket;
	public KeyDetect(User user, DatagramSocket rSocket)
	{
		nxtBot = user;
		nxtSocket = rSocket;
	}
	boolean up = false;
	@Override
	public void run() {
		JTextField textField = new JTextField();
		textField.addKeyListener(new MKeyListener());
		JFrame jFrame = new JFrame();
		jFrame.add(textField);
		jFrame.setSize(500,500);
		jFrame.setVisible(true);
	}


	class MKeyListener extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent event)
		{
			if(event.getKeyCode() == KeyEvent.VK_UP)
			{
				nxtBot.send(nxtSocket,"backward");
				up = true;
				System.out.println("Up Arrow key pressed");
			}
			if(event.getKeyCode() == KeyEvent.VK_DOWN)
			{
				nxtBot.send(nxtSocket,"moveforward");
				up = true;
				System.out.println("Reverse key pressed");
			}
			if(event.getKeyCode() == KeyEvent.VK_RIGHT)
			{
				nxtBot.send(nxtSocket,"turnright");
				up = true;
				System.out.println("Right Arrow key pressed");
			}
			if(event.getKeyCode() == KeyEvent.VK_LEFT)
			{
				nxtBot.send(nxtSocket,"turnleft");
				up = true;
				System.out.println("Left Arrow key pressed");
			}


		}
		public void keyTyped(KeyEvent event)
		{
			//Not used
		}

		public void keyReleased(KeyEvent event)
		{
			//not used
		}
	}
}

public class JavaServer {
	public static final int PORT = 8002;
	public static void main(String[] args) {
		Users allUserList = new Users();
		DatagramPacket dp = null;
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket(PORT);
			new Thread(new Receive(ds, allUserList)).start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}