import { useState, useContext } from "react";
import { Box, Typography } from "@mui/material";
import PersonIcon from "@mui/icons-material/Person";
import logo from "./logo.png";
import Sidebar from "./Sidebar";
import { AuthContext, AuthIsSignedIn } from "./../contexts/authContext";

export default function Header() {
    const userInfo = useContext(AuthContext);
    const [userName, setUsername] = useState("");

    const getSessionDetails = new Promise((resolve, reject) => {
        resolve(userInfo.getSession());
    });
    getSessionDetails.then(
        (data) =>
            setUsername(
                data?.idToken?.payload["cognito:username"] ||
                    data?.idToken?.payload?.email?.split("@")[0] ||
                    ""
            ),
        () => setUsername("")
    );

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
                pl={{ xs: "60px", md: "80px", lg: "100px" }}
                pr={{ xs: "20px", md: "35px" }}
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
                <div>
                    <Typography
                        variant="h1"
                        align="left"
                        valign="bottom"
                        sx={{
                            fontWeight: 700,
                            color: "#FD6552",
                            fontSize: { xs: "26px", lg: "2.25rem" },
                            pl: { xs: "10px", md: "20px", lg: "30px" },
                        }}
                    >
                        SCOOT
                    </Typography>
                </div>
                {userName && (
                    <Typography
                        variant="p"
                        component="p"
                        align="right"
                        valign="bottom"
                        sx={{
                            fontWeight: "bold",
                            textTransform: "capitalize",
                            fontSize: "1.15rem",
                            width: "100%",
                        }}
                    >
                        Welcome{" "}
                        <span style={{ color: "#FD6552" }}>{userName}</span>
                        <PersonIcon
                            sx={{
                                fontSize: "2rem",
                                color: "#FD6552",
                                verticalAlign: "bottom",
                            }}
                        ></PersonIcon>
                    </Typography>
                )}
            </Box>
        </Box>
    );
}
