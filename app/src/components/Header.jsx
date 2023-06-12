import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import logo from "./logo.png";
import Sidebar from "./Sidebar";
import { AuthIsSignedIn } from "./../contexts/authContext";

export default function Header() {
    return (
        <Box
            id="outer-container"
            className="header-container"
            sx={{
                mt: 0,
                background: "#fafafa",
            }}
        >
            <AuthIsSignedIn>
                <Sidebar
                    pageWrapId={"page-wrap"}
                    outerContainerId={"outer-container"}
                />
            </AuthIsSignedIn>
            <Box
                container
                px={{ xs: "50px", md: "100px" }}
                py={"20px"}
                sx={{
                    mt: 0,
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    borderBottom: "1px solid lightgrey",
                }}
            >
                <div>
                    <img
                        src={logo}
                        alt="Logo"
                        align="right"
                        valign="bottom"
                        width="70px"
                    />
                </div>
                <div
                    style={{
                        "margin-left": "30px",
                    }}
                >
                    <Typography
                        variant="h1"
                        align="left"
                        valign="bottom"
                        sx={{
                            fontWeight: 700,
                            color: "#FD6552",
                            fontSize: { md: "26px", lg: "2.25rem" },
                        }}
                    >
                        SCOOT
                    </Typography>
                </div>
            </Box>
        </Box>
    );
}
