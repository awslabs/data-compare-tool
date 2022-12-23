import * as React from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import Button from "@mui/material/Button";
import {Box} from "@mui/material";
import {data1} from "./SchemaAndTableSelect";

const data = [
    {
        slNo:1,
        tableName: 'table1',
        tableRun: 'Run-1',
        runDate: '2022-12-14'
    },
    {
        slNo:2,
        tableName: 'table2',
        tableRun: 'Run-2',
        runDate: '2022-12-14'
    },
    {
        slNo:3,
        tableName: 'table3',
        tableRun: 'Run-3',
        runDate: '2022-12-14'
    },
];

function NewSelect(){
return(<div>
    <TableContainer component={Paper} >
        <Table sx={{ minWidth: 650 }} aria-label="simple table">
            <TableHead>
                <TableRow>
                    <TableCell>Sl No.</TableCell>
                    <TableCell align="right">Table Name</TableCell>
                    <TableCell align="right">Table Run</TableCell>
                    <TableCell align="right">Run Date</TableCell>
                    <TableCell align="center">Action</TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {data.map((row) => (
                    <TableRow
                        key={row.slNo}
                        sx={{ "&:last-child td, &:last-child th": { border: 0 } }}
                    >
                        <TableCell component="th" scope="row">
                            {row.slNo}
                        </TableCell>
                        <TableCell align="right">{row.tableName}</TableCell>
                        <TableCell align="right">{row.tableRun}</TableCell>
                        <TableCell align="right">{row.runDate}</TableCell>
                        {/*<TableCell align="right">{row.sync}</TableCell>*/}
                        {/*<TableCell align="right">{row.edit}</TableCell>*/}
                        <TableCell align="center"><Box><Button variant="contained" color="secondary">Sync</Button>       <Button variant="contained" color="success">Edit</Button></Box></TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </TableContainer>
</div>)
}

export default NewSelect;