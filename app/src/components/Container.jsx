import Header from "./Header";
import Footer from "./Footer";
import { Box } from "@mui/material";

const Container = ({ children }) => {
    return (
        <>
            <Header />
            <Box
                px={{ xs: 2 }}
                pb={2}
                pt={4}
                m={{ xs: "0 0 40px", md: "0 auto 40px" }}
                sx={{
                    maxWidth: "1400px",
                    minWidth: "700px",
                    width: "auto",
                    border: "1px solid transparent",
                    boxShadow: "0 0 20px",
                    clipPath: "inset(0 -25px 0 -25px)",
                    minHeight: "calc(100vh - 152px)",
                }}
            >
                {children}
            </Box>
            <Footer />
        </>
    );
};
export default Container;
