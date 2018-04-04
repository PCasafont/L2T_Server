/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.instancemanager;

import l2server.DatabasePool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kilian, Pere
 */
public class SurveyManager {
	private static SurveyManager instance;

	private final String GET_CURRENT_SURVEY =
			"SELECT survey_id,question,description FROM survey WHERE survey_id = (SELECT MAX(survey_id) FROM survey WHERE active = 1)";
	private final String GET_CURRENT_SURVEY_POSSIBLE_ANSWERS = "SELECT answer_id,answer FROM survey_possible_answer WHERE survey_id = ?";
	private final String GET_CURRENT_SURVEY_ANSWERS = "SELECT charId FROM survey_answer WHERE survey_id = ?";
	private final String STORE_ANSWER = "INSERT INTO survey_answer (charId,survey_id,answer_id) VALUES (?,?)";

	private int id = 0;
	private String question;
	private String description;
	private Map<Integer, String> possibleAnswers;
	private List<Integer> answers;

	private SurveyManager() {
		load();
	}

	private void load() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(GET_CURRENT_SURVEY);
			ResultSet rset = statement.executeQuery();

			if (rset.next()) {
				id = rset.getInt("survey_id");
				question = rset.getString("question");
				description = rset.getString("description");

				PreparedStatement statement2 = con.prepareStatement(GET_CURRENT_SURVEY_POSSIBLE_ANSWERS);
				statement2.setInt(1, id);
				ResultSet rset2 = statement2.executeQuery();
				Map<Integer, String> possibleAnswers = new HashMap<>();
				while (rset2.next()) {
					possibleAnswers.put(rset.getInt("answer_id"), rset.getString("answer"));
				}

				this.possibleAnswers = possibleAnswers;

				statement2 = con.prepareStatement(GET_CURRENT_SURVEY_ANSWERS);
				statement2.setInt(1, id);
				rset2 = statement2.executeQuery();
				List<Integer> answers = new ArrayList<>();
				while (rset2.next()) {
					answers.add(rset.getInt("charId"));
				}

				this.answers = answers;
			}

			rset.close();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabasePool.close(con);
		}
	}

	public boolean isActive() {
		return id > 0;
	}

	public String getQuestion() {
		return question;
	}

	public String getDescription() {
		return description;
	}

	public Integer[] getPossibleAnswerIds() {
		return (Integer[]) possibleAnswers.keySet().toArray();
	}

	public String getPossibleAnswer(int id) {
		return possibleAnswers.get(id);
	}

	public boolean playerAnswered(int playerObjId) {
		return answers.contains(playerObjId);
	}

	public boolean storeAnswer(int playerObjId, int answerIndex) {
		if (answers.contains(playerObjId)) {
			return false;
		}
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(STORE_ANSWER);
			statement.setInt(1, playerObjId);
			statement.setInt(2, answerIndex);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabasePool.close(con);
		}
		answers.add(playerObjId);
		return true;
	}

	public static SurveyManager getInstance() {
		if (instance == null) {
			instance = new SurveyManager();
		}
		return instance;
	}
}
