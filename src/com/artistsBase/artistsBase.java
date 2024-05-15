package com.artistsBase;

import java.io.*;
import java.util.Map;

import redis.clients.jedis.Jedis;

public class artistsBase {

	public static void main (String [] args) throws Exception {

		String replyFromUser;
		Jedis jedis = new Jedis("localhost", 6379);
		String ARTISTS_KEY = "artists";
		String USERS_KEY = "users";

		// read from the input
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.println("Type a name:");
			String username = inFromUser.readLine(); //read the reply
			System.out.println("(I)nsert an artist | (Q)uery an artist | (S)tatistics | e(X)it");
			replyFromUser = inFromUser.readLine(); //read the reply
			if (replyFromUser.equals("I")) {
				System.out.println("Type the artist's name:");
				String artistName = inFromUser.readLine();
				// Check if the artist already exists.
				if (jedis.hexists(ARTISTS_KEY, artistName)) {
					String existingUser = jedis.hget(ARTISTS_KEY, artistName);
					System.out.println("The given artist " + artistName + " has been already inserted by " + existingUser);
				} else {
					// Artist and user's insertion to the hash.
					jedis.hset(ARTISTS_KEY, artistName, username);
					System.out.println("Successful insertion of " + artistName + " from user " + username);
					// Increment of user's insertions.
					jedis.hincrBy(USERS_KEY, username, 1);
				}
			} else if (replyFromUser.equals("Q")) {
				System.out.println("Type the artist's name:");
				String artistName = inFromUser.readLine();
				// Check if artist already exists.
				if (jedis.hexists(ARTISTS_KEY, artistName)) {
					String existingUser = jedis.hget(ARTISTS_KEY, artistName);
					// Increase the counter of the artist.
					jedis.hincrBy(artistName, "count", 1);
					System.out.println("The artist " + artistName + " has been inserted by " + existingUser);
				} else {
					System.out.println("The artist " + artistName + " is not found.");
				}
			} else if (replyFromUser.equals("S")) {
				Map<String, String> users = jedis.hgetAll(USERS_KEY);
				for (Map.Entry<String, String> entry : users.entrySet()) {
					System.out.println("User " + entry.getKey() + " has done " + entry.getValue() + " insertions.");
				}

				// Computation of the average times that the artists has been requested.
				Map<String, String> artists = jedis.hgetAll(ARTISTS_KEY);
				int totalRequests = 0;
				for (Map.Entry<String, String> entry : artists.entrySet()) {
					if(jedis.hget(entry.getKey(), "count") != null){
						int count = Integer.parseInt(jedis.hget(entry.getKey(), "count"));
						totalRequests += count;
					}
				}
				double averageRequests = totalRequests / (double) (artists.size());
				System.out.println("The average times that the artists have been requested is: " + averageRequests);
			} else if (replyFromUser.equals("X")) {
				System.out.println("Goodbye");
				System.exit(1);
			} else {
				System.out.println(replyFromUser + "is not a valid choice, retry");
			}
		}
	}
}

