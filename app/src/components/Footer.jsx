import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

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
                &copy; {today.getFullYear()}, Amazon Web Services, Inc. or its
                affiliates. All rights reserved.
            </Typography>
        </Box>
    );
};

export default Footer;
