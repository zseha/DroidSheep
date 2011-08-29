/*
 * SystemHelper.java executed superuser commands Copyright (C) 2011 Andreas Koch
 * <koch.trier@gmail.com>
 * 
 * This software was supported by the University of Trier
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.trier.infsec.koch.droidsheep.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.util.Log;
import de.trier.infsec.koch.droidsheep.activities.ListenActivity;
import de.trier.infsec.koch.droidsheep.auth.Auth;

public class SystemHelper {

	static Process process = null;
	
	public static boolean execSUCommand(String command) {
		try {
			if (process == null || process.getOutputStream() == null) {
				process = new ProcessBuilder().command("su").start();
			}
			
			if (Constants.DEBUG) {
				Log.d(Constants.APPLICATION_TAG, "Command: " + command);
			}
			process.getOutputStream().write((command + "\n").getBytes("ASCII"));
			process.getOutputStream().flush();				
			if (Constants.DEBUG) {
				StringBuffer sb = new StringBuffer();
				while (process.getErrorStream().available() > 0) {
					sb.append((char) process.getErrorStream().read());
				}
				String s = sb.toString();
				if (!s.replaceAll(" ", "").equalsIgnoreCase("")) {
					Log.e(Constants.APPLICATION_TAG, "Error with command: " + s);
					return false;
				}
			}
			Thread.sleep(300);
			return true;
		} catch (Exception e) {
			Log.e(Constants.APPLICATION_TAG, "Error executing: " + command, e);
			return false;
		}
	}
	
	public static void execNewSUCommand(String command) {
		try {
			if (Constants.DEBUG) {
				Log.d(Constants.APPLICATION_TAG, "Command: " + command);
			}

			Process process = new ProcessBuilder().command("su").start();
			process.getOutputStream().write((command + "\n").getBytes("ASCII"));
			process.getOutputStream().flush();				
			if (Constants.DEBUG) {
				StringBuffer sb = new StringBuffer();
				while (process.getErrorStream().available() > 0) {
					sb.append((char) process.getErrorStream().read());
				}
				if (sb.toString().length() > 0) {
					Log.e(Constants.APPLICATION_TAG, "Error with command: " + sb.toString());
				}
			}
			Thread.sleep(100);
			
		} catch (Exception e) {
			Log.e(Constants.APPLICATION_TAG, "Error executing: " + command, e);
		}
	}

	public static String getDroidSheepBinaryPath(Context c) {
		return c.getFilesDir().getAbsolutePath() + File.separator + "droidsheep";
	}

	public static String getARPSpoofBinaryPath(Context c) {
		return c.getFilesDir().getAbsolutePath() + File.separator + "arpspoof";
	}

	public static void saveAuthToFile(Context c, Auth a) {
		File dir = new File(c.getFilesDir() + File.separator + "saved");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		File f = new File(c.getFilesDir() + File.separator + "saved" + File.separator + "droidsheep" + a.getId());
		try {
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(a);
			out.close();
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			bos.writeTo(new FileOutputStream(f.getAbsolutePath()));
			a.setSaved(true);
		} catch (IOException ioe) {
			Log.e("serializeObject", "error", ioe);
		}
	}
	
	public static void deleteAuthFile(Context c, Auth a) {
		if (a == null) {
			return;
		}
		File f = new File(c.getFilesDir() + File.separator + "saved" + File.separator + "droidsheep" + a.getId());
		if (f.exists()) {
			f.delete();
		}
		a.setSaved(false);
	}


	public static void readAuthFiles(Context c) {
		File f = new File(c.getFilesDir() + File.separator + "saved");
		if (!f.exists() || !f.isDirectory()) {
			Log.e(Constants.APPLICATION_TAG, c.getFilesDir() + File.separator + "saved" + " does not exist or is no folder!");
			return;
		}
		
		for (File objFile : f.listFiles()) {
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new FileInputStream(objFile));
				Auth object = (Auth) in.readObject();
				in.close();
				object.setSaved(true);
				ListenActivity.authList.add(object);
			} catch (Exception e) {
				Log.e(Constants.APPLICATION_TAG, "Error while deserialization!", e);
			}
		}
	}
}
