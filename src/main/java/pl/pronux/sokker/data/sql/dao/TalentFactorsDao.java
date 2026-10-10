package pl.pronux.sokker.data.sql.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

import pl.pronux.sokker.model.TalentFactors.Factor;

public class TalentFactorsDao {

	private Connection connection;

	public TalentFactorsDao(Connection connection) {
		this.connection = connection;
	}

	/** the stored factors, unknown names left out */
	public Map<Factor, Double> getFactors() throws SQLException {
		Map<Factor, Double> factors = new EnumMap<Factor, Double>(Factor.class);
		PreparedStatement ps = connection.prepareStatement("SELECT name, value FROM talent_factor");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String name = rs.getString(1);
			for (Factor factor : Factor.values()) {
				if (factor.getKey().equals(name)) {
					factors.put(factor, rs.getDouble(2));
				}
			}
		}
		rs.close();
		ps.close();
		return factors;
	}

	public void deleteFactors() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("DELETE FROM talent_factor");
		ps.executeUpdate();
		ps.close();
	}

	public void addFactor(Factor factor, double value) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("INSERT INTO talent_factor (name, value) VALUES (?, ?)");
		ps.setString(1, factor.getKey());
		ps.setDouble(2, value);
		ps.executeUpdate();
		ps.close();
	}
}
