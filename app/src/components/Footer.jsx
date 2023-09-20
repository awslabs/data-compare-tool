import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import logo from "./logo.png";

const Footer = () => {
    const today = new Date();

    return (
        <Box
            container
            px={{ xs: "50px", md: "100px" }}
            py={"10px"}
            sx={{
                mt: 0,
                background: "#fafafa",
                position: "fixed",
                bottom: 0,
                width: "100%",
                borderTop: "1px solid lightgrey",
                zIndex: 1,
            }}
        >
            <Typography variant="p" align="left">
                <span>
                    <img
                        src={logo}
                        alt="Logo"
                        align="left"
                        valign="bottom"
                        width="20px"
                    />
                </span>
                {today.getFullYear()}, DVT. Licensed under Apache v2.0.
            </Typography>
        </Box>
    );
};

export default Footer;
