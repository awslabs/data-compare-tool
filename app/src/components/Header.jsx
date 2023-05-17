import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import logo from "./dart-logo.jpg";

export default function Header() {
    return (
        <Box
            container
            px={{ xs: "50px", md: "100px" }}
            mb={"20px"}
            py={"20px"}
            sx={{ mt: 0, background: "#fff" }}
        >
            <div
                style={{
                    display: "inline-block",
                    width: "160px",
                    "vertical-align": "middle",
                }}
            >
                <img src={logo} alt="Logo" align="right" valign="bottom" />
            </div>
            <div
                style={{
                    display: "inline-block",
                    width: "auto",
                    "margin-left": "30px",
                }}
            >
                <Typography
                    variant="h4"
                    align="left"
                    valign="bottom"
                    sx={{
                        fontWeight: 700,
                        color: "#FD6552",
                        fontSize: { md: "26px", lg: "2.25rem" },
                    }}
                >
                    Data Validation And Remediation Tool ( DVART ){" "}
                </Typography>
            </div>
        </Box>
    );
}
