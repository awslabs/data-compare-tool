package com.datavalidationtool.service;

import com.datavalidationtool.dao.DataSource;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.request.ScheduleRequest;
import com.datavalidationtool.model.response.LastRunDetails;
import com.datavalidationtool.model.response.ScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class SchedulerService {
    @Autowired
    private ValidationService validationService;
    @Autowired
    public DataSource dataSource;
    public Logger logger = LoggerFactory.getLogger("JobScheduler");
    public ScheduleResponse addRunSchedules(ScheduleRequest scheduleRequest)  throws SQLException {
            ScheduleResponse sResponse= new ScheduleResponse();
            Timestamp endDate=scheduleRequest.getEndDate()==null ?getEndDate(scheduleRequest):scheduleRequest.getEndDate();
            String query = "INSERT INTO public.schedule_runs(chunk_column, chunk_size, data_filter,  schedule_time, source_schema, status, table_name, target_schema, unique_columns,frequency,end_date,time_interval,incremental)\n" +
                    "\tVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?);";
            Connection dbConn=null;
            PreparedStatement pst =null;
            int count=0;
            try {
                dbConn= dataSource.getDBConnection();
                for(int i=0;i<scheduleRequest.getTableNames().length;i++) {
                    pst = dbConn.prepareStatement(query);
                    pst.setString(1, scheduleRequest.getChunkColumns());
                    pst.setInt(2, scheduleRequest.getChunkSize());
                    pst.setString(3, scheduleRequest.getDataFilters());
                    pst.setTimestamp(4, scheduleRequest.getScheduleTime());
                    pst.setString(5, scheduleRequest.getSourceSchemaName());
                    pst.setString(6, "Open");
                    pst.setString(7, scheduleRequest.getTableNames()[i]);
                    pst.setString(8, scheduleRequest.getTargetSchemaName());
                    pst.setString(9, scheduleRequest.getUniqueCols());
                    pst.setString(10, (scheduleRequest.getDayFrequency()!=null && !scheduleRequest.getDayFrequency().isBlank())?scheduleRequest.getDayFrequency():scheduleRequest.getTimeFrequency());
                    pst.setTimestamp(11, endDate);
                    pst.setInt(12, scheduleRequest.getTimeOccurrence());
                    pst.setBoolean(13, scheduleRequest.isIncremental());
                    count = pst.executeUpdate();
                }
            } catch (SQLException ex) {
                logger.error("Exception while adding schedule details");
                logger.error(ex.getMessage());
            }
            finally {
                if(dbConn!=null)
                    dbConn.close();
            }
        sResponse.setCount(count);
            return sResponse;
    }

    private Timestamp getEndDate(ScheduleRequest scheduleRequest) {
    Timestamp endDate=null;
        if(scheduleRequest.isReoccurrence()){
            if(scheduleRequest.getDayFrequency()!=null && !scheduleRequest.getDayFrequency().isBlank() && scheduleRequest.getTimeOccurrence()>0){
               endDate=scheduleRequest.getScheduleEndDate();
                 }
            else if(scheduleRequest.getTimeFrequency()!=null && !scheduleRequest.getTimeFrequency().isBlank() && scheduleRequest.getNumOccurrence()>0){
                if(scheduleRequest.getTimeFrequency().equals("H"))
                    endDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusHours(scheduleRequest.getTimeOccurrence()*scheduleRequest.getNumOccurrence()));
                if(scheduleRequest.getTimeFrequency().equals("MI"))
                    endDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusMinutes(scheduleRequest.getTimeOccurrence()*scheduleRequest.getNumOccurrence()));
                 }
            else{
                endDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusMinutes(2));
            }

        }else
        {
            endDate=Timestamp.valueOf(scheduleRequest.getScheduleEndDate().toLocalDateTime().plusMinutes(2));;
        }
        return endDate;
    }

    public ArrayList<ScheduleRequest> getScheduleInfo(ScheduleRequest scheduleRequest) throws SQLException {
        RunDetails runDetails = new RunDetails();
        String query = "select * from public.schedule_runs where schedule_time >= now()- INTERVAL '15 days 5 minutes' and schedule_time <= now()+ INTERVAL '15 days 5 minutes' '";
        Connection dbConn=null;
        Statement st =null;
        ArrayList<ScheduleRequest> scheduleList=new ArrayList<ScheduleRequest>();
        try {
            dbConn= dataSource.getDBConnection();
            st=dbConn.createStatement();
            String hostName=dbConn.getMetaData().getURL();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                ScheduleRequest sRequest=new ScheduleRequest();
                sRequest.setTargetHost(hostName.substring(hostName.indexOf("jdbc:postgresql://")+18,hostName.lastIndexOf(":")));
                sRequest.setTargetDBName(hostName.substring(hostName.lastIndexOf("/")+1,hostName.lastIndexOf("?")));
                sRequest.setChunkColumns(rs.getString("chunk_column"));
                sRequest.setChunkSize(rs.getInt("chunk_size"));
                sRequest.setDataFilters(rs.getString("data_filter"));
                sRequest.setScheduleId(rs.getLong("id"));
                sRequest.setRunId(rs.getString("run_id"));
                sRequest.setScheduleTime(rs.getTimestamp("schedule_time"));
                sRequest.setSourceSchemaName(rs.getString("source_schema"));
                sRequest.setStatus(rs.getString("status"));
                sRequest.setTableNames(new String[]{rs.getString("table_name")});
                sRequest.setTargetSchemaName(rs.getString("target_schema"));
                sRequest.setUniqueCols(rs.getString("unique_columns"));
                sRequest.setDuration(rs.getInt("duration"));
                scheduleList.add(sRequest);
            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getScheduleInfo");
            logger.error(ex.getMessage());
        }
        finally {
            if(dbConn!=null)
                dbConn.close();
        }
        return scheduleList;
    }

    public LastRunDetails getScheduleJobRuns(ScheduleRequest scheduleRequest) {
        return null;
    }
}
