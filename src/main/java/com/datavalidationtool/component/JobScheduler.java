package com.datavalidationtool.component;

import com.datavalidationtool.dao.DataSource;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.request.ScheduleRequest;
import com.datavalidationtool.model.response.ScheduleResponse;
import com.datavalidationtool.service.SchedulerService;
import com.datavalidationtool.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JobScheduler {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "MM/dd/yyyy HH:mm:ss");
    @Autowired
    private ValidationService validationService;
    @Autowired
    public DataSource dataSource;
    @Autowired
    private SchedulerService schedulerService;

    public boolean validationFlag=false;
    public Logger logger = LoggerFactory.getLogger("JobScheduler");

    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    public void performDelayedTask() throws SQLException {
        logger.info("Schedule Started");
        List<ScheduleRequest> schList= getSchedules();
        if(schList!=null && schList.size()>0 && !validationFlag) {
            for (int i = 0; i < schList.size(); i++) {
                logger.info("Scheduled Validation started");
                validationFlag = true;
                String runId = validationService.validateData(schList.get(i));
                validationFlag = false;
                ScheduleRequest scheduleRequest= schList.get(i);
                updateScheduleStatus(scheduleRequest);
                scheduleRequest.setEndDate(scheduleRequest.getScheduleEndDate());
                //scheduleRequest.setScheduleTime(scheduleRequest.getScheduleEndDate());
                schedulerService.addRunSchedules(scheduleRequest);
                logger.info("Scheduled Validation completed");
            }
        }
    }

    private void updateScheduleStatus(ScheduleRequest scheduleRequest) throws SQLException {
        ScheduleResponse sResponse= new ScheduleResponse();
        RunDetails runDetails = new RunDetails();
        String query = "update public.schedule_runs set status=?,run_id=?,schedule_time=? where id=?" ;
        Connection dbConn=null;
        PreparedStatement pst =null;
        Timestamp scheduledDate=getScheduledDate(scheduleRequest);
        int count=0;
        try {
            dbConn= dataSource.getDBConnection();
                pst = dbConn.prepareStatement(query);
                pst.setString(1, "Complete");
                pst.setString(2, scheduleRequest.getRunId());
                pst.setTimestamp(3, scheduleRequest.getScheduleTime());
                pst.setLong(4, scheduleRequest.getScheduleId());
                count = pst.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Exception while adding schedule details");
            logger.error(ex.getMessage());
        }
        finally {
            if(dbConn!=null)
                dbConn.close();
        }
        scheduleRequest.setScheduleTime(scheduledDate);
    }

    private Timestamp getScheduledDate(ScheduleRequest scheduleRequest) {Timestamp scheduledDate =scheduleRequest.getScheduleTime();
        if(scheduleRequest.getDayFrequency()!=null && scheduleRequest.getDayFrequency().equals("D"))
            scheduledDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusDays(1));
        else if(scheduleRequest.getDayFrequency()!=null && scheduleRequest.getDayFrequency().equals("W"))
            scheduledDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusWeeks(1));
        else if(scheduleRequest.getDayFrequency()!=null && scheduleRequest.getDayFrequency().equals("M"))
            scheduledDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusMonths(1));
        else if(scheduleRequest.getDayFrequency()!=null && scheduleRequest.getDayFrequency().equals("H") || (scheduleRequest.getTimeFrequency()!=null && scheduleRequest.getTimeFrequency().equals("H")))
            scheduledDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusHours(scheduleRequest.getTimeOccurrence()));
        else if(scheduleRequest.getDayFrequency()!=null && scheduleRequest.getDayFrequency().equals("MI")|| (scheduleRequest.getTimeFrequency()!=null && scheduleRequest.getTimeFrequency().equals("MI")))
            scheduledDate=Timestamp.valueOf(scheduleRequest.getScheduleTime().toLocalDateTime().plusMinutes(scheduleRequest.getTimeOccurrence()));
    return scheduledDate;
    }

    private List<ScheduleRequest> getSchedules() throws SQLException {

        RunDetails runDetails = new RunDetails();
        String query = "select * from public.schedule_runs where schedule_time >= now()- INTERVAL '5 minutes' and schedule_time <= now() and status ='Open' and end_date >= schedule_time";
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
                sRequest.setScheduleEndDate(rs.getTimestamp("end_date"));
                sRequest.setDayFrequency(rs.getString("frequency"));
                sRequest.setTimeOccurrence(rs.getInt("time_interval"));
                sRequest.setIncremental(rs.getBoolean("incremental"));
                scheduleList.add(sRequest);
            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getSchedules");
            logger.error(ex.getMessage());
        }
        finally {
            if(dbConn!=null)
                dbConn.close();
        }
        return scheduleList;
    }

}
